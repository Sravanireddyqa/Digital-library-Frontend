<?php
/**
 * Change Password API
 * Updates user password after verifying old password
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $data = json_decode(file_get_contents('php://input'), true);

    if (!isset($data['user_id']) || !isset($data['old_password']) || !isset($data['new_password'])) {
        respond(false, 'User ID, old password, and new password are required');
    }

    $userId = intval($data['user_id']);
    $oldPassword = $data['old_password'];
    $newPassword = $data['new_password'];

    if (strlen($newPassword) < 6) {
        respond(false, 'New password must be at least 6 characters');
    }

    $conn = getConnection();

    // Get current user password
    $stmt = $conn->prepare("SELECT password FROM users WHERE id = ?");
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $result = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if (!$result) {
        respond(false, 'User not found');
    }

    // Verify old password
    // Check if stored as hash or plain text
    $storedPassword = $result['password'];
    $passwordValid = false;

    if (password_verify($oldPassword, $storedPassword)) {
        $passwordValid = true;
    } else if ($storedPassword === $oldPassword) {
        // Plain text comparison for legacy passwords
        $passwordValid = true;
    }

    if (!$passwordValid) {
        respond(false, 'Current password is incorrect');
    }

    // Update to new password (hashed)
    $hashedPassword = password_hash($newPassword, PASSWORD_DEFAULT);
    $updateStmt = $conn->prepare("UPDATE users SET password = ? WHERE id = ?");
    $updateStmt->bind_param("si", $hashedPassword, $userId);

    if ($updateStmt->execute()) {
        respond(true, 'Password changed successfully');
    } else {
        respond(false, 'Failed to update password');
    }

    $updateStmt->close();
    $conn->close();

} catch (Exception $e) {
    error_log("Change Password Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>