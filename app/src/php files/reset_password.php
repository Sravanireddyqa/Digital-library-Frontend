<?php
/**
 * Reset Password API - Step 3
 * Resets password after OTP verification
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $data = json_decode(file_get_contents('php://input'), true);

    $email = filter_var(trim($data['email'] ?? ''), FILTER_VALIDATE_EMAIL);
    $resetToken = trim($data['reset_token'] ?? '');
    $newPassword = trim($data['new_password'] ?? '');

    if (!$email || !$resetToken || !$newPassword) {
        respond(false, 'Email, reset token, and new password are required');
    }

    if (strlen($newPassword) < 6) {
        respond(false, 'Password must be at least 6 characters');
    }

    $conn = getConnection();

    // Verify reset token
    $stmt = $conn->prepare("SELECT * FROM password_resets WHERE email = ? AND otp = ? AND used = 0 AND expiry > NOW()");
    $stmt->bind_param("ss", $email, $resetToken);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        respond(false, 'Invalid or expired reset token. Please try again.');
    }

    // Hash new password
    $hashedPassword = password_hash($newPassword, PASSWORD_DEFAULT);

    // Update password
    $stmt = $conn->prepare("UPDATE users SET password = ? WHERE email = ?");
    $stmt->bind_param("ss", $hashedPassword, $email);
    $stmt->execute();

    if ($stmt->affected_rows > 0) {
        // Mark token as used
        $stmt = $conn->prepare("UPDATE password_resets SET used = 1 WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();

        respond(true, 'Password reset successfully! You can now login with your new password.');
    } else {
        respond(false, 'Failed to reset password. Please try again.');
    }

    $conn->close();

} catch (Exception $e) {
    error_log("Reset Password Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>