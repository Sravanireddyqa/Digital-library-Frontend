<?php
/**
 * Debug FCM Push Notifications
 * Access this file in browser to test push notification sending
 */

require_once 'db.php';
require_once 'send_notification.php';

setHeaders();

// Get test parameters
$userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

echo "<h2>FCM Push Notification Debug</h2>";

// Step 1: Check if service account file exists
$serviceAccountFile = __DIR__ . '/firebase-service-account.json';
echo "<h3>1. Service Account File</h3>";
if (file_exists($serviceAccountFile)) {
    echo "<p style='color:green'>✓ Service account file exists</p>";

    $serviceAccount = json_decode(file_get_contents($serviceAccountFile), true);
    if ($serviceAccount) {
        echo "<p style='color:green'>✓ Service account file is valid JSON</p>";
        echo "<p>Project ID: " . ($serviceAccount['project_id'] ?? 'NOT FOUND') . "</p>";
        echo "<p>Client Email: " . ($serviceAccount['client_email'] ?? 'NOT FOUND') . "</p>";

        // Check if private key exists
        if (isset($serviceAccount['private_key'])) {
            echo "<p style='color:green'>✓ Private key present</p>";
        } else {
            echo "<p style='color:red'>✗ Private key MISSING</p>";
        }
    } else {
        echo "<p style='color:red'>✗ Failed to parse service account JSON</p>";
    }
} else {
    echo "<p style='color:red'>✗ Service account file NOT FOUND at: $serviceAccountFile</p>";
}

// Step 2: Check FCM tokens
echo "<h3>2. FCM Tokens in Database</h3>";
$conn = getConnection();

if ($userId > 0) {
    $stmt = $conn->prepare("SELECT t.*, u.name, u.email FROM fcm_tokens t JOIN users u ON t.user_id = u.id WHERE t.user_id = ?");
    $stmt->bind_param("i", $userId);
} else {
    $stmt = $conn->prepare("SELECT t.*, u.name, u.email FROM fcm_tokens t JOIN users u ON t.user_id = u.id LIMIT 10");
}
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>User ID</th><th>Name</th><th>Email</th><th>Token (first 50 chars)</th><th>Created</th></tr>";

    while ($row = $result->fetch_assoc()) {
        echo "<tr>";
        echo "<td>" . $row['user_id'] . "</td>";
        echo "<td>" . $row['name'] . "</td>";
        echo "<td>" . $row['email'] . "</td>";
        echo "<td>" . substr($row['token'], 0, 50) . "...</td>";
        echo "<td>" . $row['created_at'] . "</td>";
        echo "</tr>";
    }
    echo "</table>";
} else {
    echo "<p style='color:red'>✗ No FCM tokens found!</p>";
}

// Step 3: Test getting access token
echo "<h3>3. Firebase Access Token</h3>";
$accessToken = getFirebaseAccessToken();
if ($accessToken) {
    echo "<p style='color:green'>✓ Access token obtained successfully</p>";
    echo "<p>Token (first 50 chars): " . substr($accessToken, 0, 50) . "...</p>";
} else {
    echo "<p style='color:red'>✗ Failed to get access token - check error logs</p>";
}

// Step 4: Send test notification if user_id and send=1 provided
if ($userId > 0 && isset($_GET['send']) && $_GET['send'] == '1') {
    echo "<h3>4. Sending Test Push Notification</h3>";

    // Get user's FCM token
    $stmt = $conn->prepare("SELECT token FROM fcm_tokens WHERE user_id = ? LIMIT 1");
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($row = $result->fetch_assoc()) {
        $token = $row['token'];
        echo "<p>Sending to token: " . substr($token, 0, 50) . "...</p>";

        // Send test message
        $notification = [
            'title' => '🔔 Test Push Notification',
            'body' => 'This is a test message from LibraryAI debug script at ' . date('H:i:s')
        ];

        $data = [
            'type' => 'test',
            'notification_id' => '999',
            'title' => $notification['title'],
            'message' => $notification['body']
        ];

        $result = sendFCMMessage($token, $notification, $data);

        if ($result) {
            echo "<p style='color:green'>✓ Push notification sent successfully!</p>";
        } else {
            echo "<p style='color:red'>✗ Push notification FAILED - check PHP error logs</p>";
        }
    } else {
        echo "<p style='color:red'>✗ No FCM token found for user $userId</p>";
    }
}

$conn->close();

// Usage instructions
echo "<hr>";
echo "<h3>Usage</h3>";
echo "<ul>";
echo "<li>View all tokens: <a href='?'>?</a></li>";
echo "<li>View user's tokens: <a href='?user_id=33'>?user_id=33</a></li>";
echo "<li>Send test push to user: <a href='?user_id=33&send=1'>?user_id=33&send=1</a></li>";
echo "</ul>";
echo "<p><strong>Check PHP error log:</strong> C:\\xampp\\apache\\logs\\error.log</p>";
?>