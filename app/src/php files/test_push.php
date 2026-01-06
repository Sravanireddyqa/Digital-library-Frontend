<?php
require_once 'send_notification.php';

// Test Push Notification Script
// Usage: Open in browser: http://10.36.207.135/digitallibrary_API/test_push.php

// Get user's FCM token from database
$userId = 33; // Change this to your test user ID
$conn = new mysqli("localhost", "root", "", "digitallibrary");

if ($conn->connect_error) {
    die("❌ Connection failed: " . $conn->connect_error);
}

$result = $conn->query("SELECT token FROM fcm_tokens WHERE user_id = $userId LIMIT 1");

if ($result && $row = $result->fetch_assoc()) {
    $token = $row['token'];

    echo "<h2>🔔 Testing Push Notification</h2>";
    echo "<p>User ID: $userId</p>";
    echo "<p>FCM Token: " . substr($token, 0, 50) . "...</p>";
    echo "<hr>";

    // Test notification
    $title = "🎉 Test Push Notification";
    $message = "This is a test push notification from your Digital Library server!";
    $data = [
        'type' => 'general_announcement',
        'click_action' => 'OPEN_NOTIFICATIONS',
        'timestamp' => date('Y-m-d H:i:s')
    ];

    $success = sendFCMMessage($token, $title, $message, $data);

    if ($success) {
        echo "<h3 style='color: green;'>✅ Push notification sent successfully!</h3>";
        echo "<p>Check your device - you should receive a notification</p>";

        // Also save to database
        $stmt = $conn->prepare("INSERT INTO notifications (user_id, type, title, message, created_at) VALUES (?, ?, ?, ?, NOW())");
        $type = 'general_announcement';
        $stmt->bind_param("isss", $userId, $type, $title, $message);
        $stmt->execute();

        echo "<p>✅ Also saved to database for in-app notifications</p>";
    } else {
        echo "<h3 style='color: red;'>❌ Failed to send push notification</h3>";
        echo "<p>Check Firebase Server Key configuration in send_notification.php</p>";
    }
} else {
    echo "<h3 style='color: red;'>❌ No FCM token found for user $userId</h3>";
    echo "<p>Make sure the user has logged in at least once to register their FCM token</p>";
}

$conn->close();
?>