<?php
/**
 * Register FCM Token
 * ===================
 * Save or update user's Firebase Cloud Messaging token
 */

require_once 'db.php';
setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(false, 'POST method required');
}

$input = getInput();

// Validate inputs
if (empty($input['user_id']) || empty($input['fcm_token'])) {
    respond(false, 'Missing required fields: user_id and fcm_token');
}

$userId = intval($input['user_id']);
$fcmToken = $input['fcm_token'];
$deviceInfo = isset($input['device_info']) ? $input['device_info'] : null;

$conn = getConnection();

try {
    // Check if token already exists for this user
    $stmt = $conn->prepare("SELECT id FROM fcm_tokens WHERE user_id = ? AND token = ?");
    $stmt->bind_param("is", $userId, $fcmToken);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        // Token already exists, update timestamp
        $stmt = $conn->prepare("UPDATE fcm_tokens SET device_info = ?, last_updated = NOW() WHERE user_id = ? AND token = ?");
        $stmt->bind_param("sis", $deviceInfo, $userId, $fcmToken);
        $stmt->execute();

        respond(true, 'FCM token updated successfully');
    } else {
        // Insert new token
        $stmt = $conn->prepare("INSERT INTO fcm_tokens (user_id, token, device_info) VALUES (?, ?, ?)");
        $stmt->bind_param("iss", $userId, $fcmToken, $deviceInfo);

        if ($stmt->execute()) {
            respond(true, 'FCM token registered successfully');
        } else {
            respond(false, 'Failed to register FCM token');
        }
    }
} catch (Exception $e) {
    respond(false, 'Error: ' . $e->getMessage());
} finally {
    $conn->close();
}
?>