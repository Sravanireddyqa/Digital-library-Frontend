<?php
/**
 * FCM Debug Script
 * Tests if push notifications are working
 */

require_once 'db.php';

setHeaders();

echo "<h2>🔔 FCM Push Notification Debugger</h2>";
echo "<hr>";

// 1. Check if firebase-service-account.json exists
echo "<h3>1. Firebase Service Account File</h3>";
$serviceAccountFile = __DIR__ . '/firebase-service-account.json';
if (file_exists($serviceAccountFile)) {
    $content = file_get_contents($serviceAccountFile);
    $sa = json_decode($content, true);
    if ($sa && isset($sa['project_id'])) {
        echo "✅ File exists and valid<br>";
        echo "Project ID: <b>" . $sa['project_id'] . "</b><br>";
        echo "Client Email: <b>" . substr($sa['client_email'], 0, 30) . "...</b><br>";
    } else {
        echo "❌ File exists but INVALID JSON<br>";
    }
} else {
    echo "❌ File NOT FOUND: $serviceAccountFile<br>";
    echo "<p style='color:red'>You need to download firebase-service-account.json from Firebase Console:</p>";
    echo "<ol>";
    echo "<li>Go to Firebase Console > Project Settings > Service Accounts</li>";
    echo "<li>Click 'Generate new private key'</li>";
    echo "<li>Rename the downloaded file to 'firebase-service-account.json'</li>";
    echo "<li>Copy it to: $serviceAccountFile</li>";
    echo "</ol>";
}

// 2. Check fcm_tokens table
echo "<h3>2. FCM Tokens Table</h3>";
try {
    $conn = getConnection();
    $result = $conn->query("SHOW TABLES LIKE 'fcm_tokens'");
    if ($result->num_rows > 0) {
        echo "✅ Table exists<br>";

        // Check if there are any tokens
        $tokenResult = $conn->query("SELECT COUNT(*) as count FROM fcm_tokens");
        $tokenCount = $tokenResult->fetch_assoc()['count'];
        echo "Total tokens registered: <b>$tokenCount</b><br>";

        if ($tokenCount > 0) {
            $tokensResult = $conn->query("SELECT ft.*, u.name, u.email FROM fcm_tokens ft 
                                          LEFT JOIN users u ON ft.user_id = u.id 
                                          ORDER BY ft.id DESC LIMIT 5");
            echo "<br><b>Recent tokens:</b><br>";
            echo "<table border='1' cellpadding='5'>";
            echo "<tr><th>User</th><th>Email</th><th>Token (truncated)</th><th>Device</th><th>Updated</th></tr>";
            while ($row = $tokensResult->fetch_assoc()) {
                echo "<tr>";
                echo "<td>" . $row['name'] . "</td>";
                echo "<td>" . $row['email'] . "</td>";
                echo "<td>" . substr($row['token'], 0, 30) . "...</td>";
                echo "<td>" . $row['device_info'] . "</td>";
                echo "<td>" . $row['updated_at'] . "</td>";
                echo "</tr>";
            }
            echo "</table>";
        } else {
            echo "<p style='color:orange'>⚠️ No tokens registered yet. Users need to logout and login again.</p>";
        }
    } else {
        echo "❌ Table does NOT exist<br>";
        echo "<p>Run this SQL to create it:</p>";
        echo "<pre>CREATE TABLE fcm_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token TEXT NOT NULL,
    device_info VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);</pre>";
    }
    $conn->close();
} catch (Exception $e) {
    echo "❌ Database error: " . $e->getMessage();
}

// 3. Check notifications table
echo "<h3>3. Notifications Table</h3>";
try {
    $conn = getConnection();
    $result = $conn->query("SHOW TABLES LIKE 'notifications'");
    if ($result->num_rows > 0) {
        echo "✅ Table exists<br>";

        $notifResult = $conn->query("SELECT COUNT(*) as count FROM notifications");
        $notifCount = $notifResult->fetch_assoc()['count'];
        echo "Total notifications: <b>$notifCount</b><br>";
    } else {
        echo "❌ Table does NOT exist<br>";
    }
    $conn->close();
} catch (Exception $e) {
    echo "❌ Database error: " . $e->getMessage();
}

// 4. Test sending notification (if user_id provided)
echo "<h3>4. Test Send Notification</h3>";
if (isset($_GET['user_id']) && isset($_GET['test'])) {
    $userId = intval($_GET['user_id']);
    echo "Attempting to send test notification to user ID: $userId<br>";

    require_once 'send_notification.php';
    $result = sendNotification($userId, 'general_announcement', 'Test Notification', 'This is a test push notification from debug script!');

    if ($result) {
        echo "✅ Notification sent successfully! Check your phone.<br>";
    } else {
        echo "❌ Failed to send notification. Check PHP error logs.<br>";
    }
} else {
    echo "<p>To test sending a notification, add ?user_id=X&test=1 to the URL</p>";
    echo "<p>Example: <a href='?user_id=1&test=1'>test_fcm.php?user_id=1&test=1</a></p>";
}

// 5. Check PHP error log location
echo "<h3>5. PHP Error Log</h3>";
$errorLog = ini_get('error_log');
echo "Error log location: <b>" . ($errorLog ?: "Default (check Apache error.log)") . "</b><br>";
echo "<p>Check this file for FCM errors if notifications aren't working.</p>";

echo "<hr>";
echo "<p><i>Debug script completed at " . date('Y-m-d H:i:s') . "</i></p>";
?>