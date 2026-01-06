<?php
/**
 * Send Notification Helper
 * =========================
 * Core function to create and send notifications
 * Uses Firebase Cloud Messaging V1 API
 */

require_once 'db.php';

// Firebase Configuration - Auto-detect project ID from service account
define('FIREBASE_SERVICE_ACCOUNT_FILE', __DIR__ . '/firebase-service-account.json');

// Get project ID from service account file
function getFirebaseProjectId()
{
    static $projectId = null;
    if ($projectId === null) {
        if (file_exists(FIREBASE_SERVICE_ACCOUNT_FILE)) {
            $sa = json_decode(file_get_contents(FIREBASE_SERVICE_ACCOUNT_FILE), true);
            $projectId = $sa['project_id'] ?? 'digitallibrary2-c708f';
            error_log("FCM Debug: Using project ID: " . $projectId);
        } else {
            $projectId = 'digitallibrary2-c708f';
            error_log("FCM Debug: Service account file not found, using default project ID");
        }
    }
    return $projectId;
}

/**
 * Send notification to user(s)
 * 
 * @param int|array $userIds - Single user ID or array of user IDs
 * @param string $type - Notification type (see NotificationType enum in Android)
 * @param string $title - Notification title
 * @param string $message - Notification message
 * @param array $data - Optional additional data
 * @return bool - Success status
 */
function sendNotification($userIds, $type, $title, $message, $data = null)
{
    $conn = getConnection();

    // Convert single user ID to array
    if (!is_array($userIds)) {
        $userIds = [$userIds];
    }

    $success = true;

    foreach ($userIds as $userId) {
        try {
            // Insert notification into database
            $dataJson = $data ? json_encode($data) : null;

            $stmt = $conn->prepare("INSERT INTO notifications (user_id, type, title, message, data) VALUES (?, ?, ?, ?, ?)");
            $stmt->bind_param("issss", $userId, $type, $title, $message, $dataJson);

            if ($stmt->execute()) {
                $notificationId = $conn->insert_id;

                // Send push notification via FCM
                sendFCMNotification($userId, $type, $title, $message, $data, $notificationId);
            } else {
                $success = false;
                error_log("Failed to insert notification for user $userId");
            }
        } catch (Exception $e) {
            $success = false;
            error_log("Error sending notification to user $userId: " . $e->getMessage());
        }
    }

    $conn->close();
    return $success;
}

/**
 * Send FCM push notification
 */
function sendFCMNotification($userId, $type, $title, $message, $data, $notificationId)
{
    $conn = getConnection();

    // Get user's FCM tokens
    $stmt = $conn->prepare("SELECT token FROM fcm_tokens WHERE user_id = ?");
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $result = $stmt->get_result();

    $tokens = [];
    while ($row = $result->fetch_assoc()) {
        $tokens[] = $row['token'];
    }

    $conn->close();

    if (empty($tokens)) {
        error_log("No FCM tokens found for user $userId");
        return false;
    }

    // Prepare FCM payload
    $notification = [
        'title' => $title,
        'body' => $message
    ];

    $dataPayload = [
        'type' => $type,
        'notification_id' => (string) $notificationId,
        'title' => $title,
        'message' => $message
    ];

    if ($data) {
        $dataPayload = array_merge($dataPayload, $data);
    }

    // Send to each token
    foreach ($tokens as $token) {
        sendFCMMessage($token, $notification, $dataPayload);
    }

    return true;
}

/**
 * Send FCM message using Firebase Cloud Messaging V1 API
 */
function sendFCMMessage($token, $notification, $data)
{
    // Get access token
    $accessToken = getFirebaseAccessToken();

    if (!$accessToken) {
        error_log("Failed to get Firebase access token");
        return false;
    }

    $url = 'https://fcm.googleapis.com/v1/projects/' . getFirebaseProjectId() . '/messages:send';

    error_log("FCM Debug: Sending to URL: " . $url);
    error_log("FCM Debug: Token: " . substr($token, 0, 30) . "...");

    // Convert all data values to strings (FCM V1 requirement)
    $stringData = [];
    foreach ($data as $key => $value) {
        $stringData[$key] = is_array($value) ? json_encode($value) : (string) $value;
    }

    $message = [
        'message' => [
            'token' => $token,
            'notification' => [
                'title' => $notification['title'],
                'body' => $notification['body']
            ],
            'data' => $stringData,
            'android' => [
                'priority' => 'high',
                'notification' => [
                    'sound' => 'default',
                    'channel_id' => 'digitallibrary_notifications'
                ]
            ]
        ]
    ];

    $headers = [
        'Authorization: Bearer ' . $accessToken,
        'Content-Type: application/json'
    ];

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($message));

    $result = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    if ($result === FALSE) {
        error_log('FCM V1 Send Error: ' . curl_error($ch));
    } else {
        $resultData = json_decode($result, true);

        if ($httpCode !== 200) {
            error_log('FCM V1 Send Failed (HTTP ' . $httpCode . '): ' . $result);

            // Handle invalid tokens
            if (isset($resultData['error']['details'])) {
                foreach ($resultData['error']['details'] as $detail) {
                    if (
                        isset($detail['errorCode']) &&
                        in_array($detail['errorCode'], ['UNREGISTERED', 'INVALID_ARGUMENT'])
                    ) {
                        removeInvalidFCMToken($token);
                    }
                }
            }
        } else {
            error_log('FCM V1 Send Success: ' . $result);
        }
    }

    curl_close($ch);
    return $httpCode === 200;
}

/**
 * Get Firebase Access Token using Service Account
 */
function getFirebaseAccessToken()
{
    static $cachedToken = null;
    static $tokenExpiry = 0;

    // Return cached token if still valid (with 5 min buffer)
    if ($cachedToken && time() < ($tokenExpiry - 300)) {
        return $cachedToken;
    }

    // Read service account file
    $serviceAccountFile = FIREBASE_SERVICE_ACCOUNT_FILE;

    if (!file_exists($serviceAccountFile)) {
        error_log("Firebase service account file not found: $serviceAccountFile");
        return null;
    }

    $serviceAccount = json_decode(file_get_contents($serviceAccountFile), true);

    if (!$serviceAccount) {
        error_log("Failed to parse Firebase service account file");
        return null;
    }

    // Create JWT
    $now = time();
    $expiry = $now + 3600; // 1 hour

    $header = [
        'alg' => 'RS256',
        'typ' => 'JWT'
    ];

    $payload = [
        'iss' => $serviceAccount['client_email'],
        'sub' => $serviceAccount['client_email'],
        'aud' => 'https://oauth2.googleapis.com/token',
        'iat' => $now,
        'exp' => $expiry,
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging'
    ];

    $headerEncoded = base64UrlEncode(json_encode($header));
    $payloadEncoded = base64UrlEncode(json_encode($payload));

    $signatureInput = $headerEncoded . '.' . $payloadEncoded;

    // Sign with private key
    $privateKey = openssl_pkey_get_private($serviceAccount['private_key']);
    if (!$privateKey) {
        error_log("Failed to load private key from service account");
        return null;
    }

    $signature = '';
    if (!openssl_sign($signatureInput, $signature, $privateKey, OPENSSL_ALGO_SHA256)) {
        error_log("Failed to sign JWT");
        return null;
    }

    $jwt = $signatureInput . '.' . base64UrlEncode($signature);

    // Exchange JWT for access token
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, 'https://oauth2.googleapis.com/token');
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query([
        'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
        'assertion' => $jwt
    ]));

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode !== 200) {
        error_log("Failed to get access token: HTTP $httpCode - $response");
        return null;
    }

    $tokenData = json_decode($response, true);

    if (!isset($tokenData['access_token'])) {
        error_log("No access token in response: $response");
        return null;
    }

    // Cache the token
    $cachedToken = $tokenData['access_token'];
    $tokenExpiry = $now + ($tokenData['expires_in'] ?? 3600);

    return $cachedToken;
}

/**
 * Base64 URL encode (JWT compatible)
 */
function base64UrlEncode($data)
{
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

/**
 * Remove invalid FCM token from database
 */
function removeInvalidFCMToken($token)
{
    $conn = getConnection();
    $stmt = $conn->prepare("DELETE FROM fcm_tokens WHERE token = ?");
    $stmt->bind_param("s", $token);
    $stmt->execute();
    $conn->close();
    error_log("Removed invalid FCM token: " . substr($token, 0, 20) . "...");
}

/**
 * Send broadcast notification to all users
 */
function sendBroadcastNotification($type, $title, $message, $data = null)
{
    $conn = getConnection();

    // Get all user IDs
    $result = $conn->query("SELECT id FROM users WHERE status = 'active'");
    $userIds = [];

    while ($row = $result->fetch_assoc()) {
        $userIds[] = $row['id'];
    }

    $conn->close();

    return sendNotification($userIds, $type, $title, $message, $data);
}
?>