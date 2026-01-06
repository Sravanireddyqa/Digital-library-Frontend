<?php
/**
 * Verify OTP API - Step 2
 * Verifies the OTP entered by user
 */

require_once 'db.php';

// Set timezone to match server
date_default_timezone_set('Asia/Kolkata');

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $data = json_decode(file_get_contents('php://input'), true);

    $email = filter_var(trim($data['email'] ?? ''), FILTER_VALIDATE_EMAIL);
    $otp = trim($data['otp'] ?? '');

    if (!$email || !$otp) {
        respond(false, 'Email and OTP are required');
    }

    if (!preg_match('/^\d{6}$/', $otp)) {
        respond(false, 'Invalid OTP format');
    }

    $conn = getConnection();

    // Check OTP - get the latest one for this email
    $stmt = $conn->prepare("SELECT * FROM password_resets WHERE email = ? AND otp = ? AND used = 0 ORDER BY id DESC LIMIT 1");
    $stmt->bind_param("ss", $email, $otp);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        // Check if OTP exists but might be expired or used
        $stmt2 = $conn->prepare("SELECT * FROM password_resets WHERE email = ? AND otp = ? ORDER BY id DESC LIMIT 1");
        $stmt2->bind_param("ss", $email, $otp);
        $stmt2->execute();
        $result2 = $stmt2->get_result();

        if ($result2->num_rows > 0) {
            $row = $result2->fetch_assoc();
            if ($row['used'] == 1) {
                respond(false, 'OTP has already been used. Please request a new one.');
            } else {
                respond(false, 'OTP has expired. Please request a new one.');
            }
        } else {
            respond(false, 'Invalid OTP');
        }
    }

    $row = $result->fetch_assoc();

    // Check expiry in PHP (more reliable than MySQL NOW())
    $expiryTime = strtotime($row['expiry']);
    $currentTime = time();

    if ($currentTime > $expiryTime) {
        respond(false, 'OTP has expired. Please request a new one.');
    }

    // Generate a reset token for password change
    $resetToken = bin2hex(random_bytes(32));
    $tokenExpiry = date('Y-m-d H:i:s', strtotime('+15 minutes'));

    // Mark OTP as used
    $stmt = $conn->prepare("UPDATE password_resets SET used = 1 WHERE id = ?");
    $stmt->bind_param("i", $row['id']);
    $stmt->execute();

    // Store reset token
    $stmt = $conn->prepare("INSERT INTO password_resets (email, otp, expiry) VALUES (?, ?, ?)");
    $stmt->bind_param("sss", $email, $resetToken, $tokenExpiry);
    $stmt->execute();

    respond(true, 'OTP verified successfully', [
        'reset_token' => $resetToken,
        'expires_in' => '15 minutes'
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("Verify OTP Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>