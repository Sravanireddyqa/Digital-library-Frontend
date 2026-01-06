<?php
/**
 * Update User API
 * Updates user status (block/unblock)
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $data = json_decode(file_get_contents('php://input'), true);

    if (!isset($data['user_id']) || !isset($data['status'])) {
        respond(false, 'Missing user_id or status');
    }

    $userId = intval($data['user_id']);
    $status = strtolower(trim($data['status']));

    if (!in_array($status, ['active', 'blocked'])) {
        respond(false, 'Invalid status. Use: active, blocked');
    }

    $conn = getConnection();

    // Check if status column exists
    $colCheck = $conn->query("SHOW COLUMNS FROM users LIKE 'status'");
    if ($colCheck->num_rows == 0) {
        $conn->query("ALTER TABLE users ADD COLUMN status ENUM('active', 'blocked') DEFAULT 'active'");
    }

    // Update user status
    $stmt = $conn->prepare("UPDATE users SET status = ? WHERE id = ?");
    $stmt->bind_param("si", $status, $userId);

    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            respond(true, 'User status updated to ' . $status);
        } else {
            respond(false, 'User not found or status unchanged');
        }
    } else {
        respond(false, 'Failed to update user: ' . $conn->error);
    }

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    error_log("Update User Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>