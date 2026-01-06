<?php
/**
 * Forgot Password API - Send OTP to Email
 * Uses PHPMailer for reliable email delivery
 */

require_once 'db.php';

// PHPMailer - You may need to install via composer or download
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;
use PHPMailer\PHPMailer\Exception;

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

    if (!$email) {
        respond(false, 'Valid email is required');
    }

    $conn = getConnection();

    // Check if email exists
    $stmt = $conn->prepare("SELECT id, name FROM users WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        respond(false, 'Email not registered');
    }

    $user = $result->fetch_assoc();
    $userName = $user['name'];

    // Generate 6-digit OTP
    $otp = str_pad(mt_rand(0, 999999), 6, '0', STR_PAD_LEFT);
    $expiry = date('Y-m-d H:i:s', strtotime('+30 minutes'));

    // Create password_resets table if not exists
    $conn->query("CREATE TABLE IF NOT EXISTS password_resets (
        id INT AUTO_INCREMENT PRIMARY KEY,
        email VARCHAR(255) NOT NULL,
        otp VARCHAR(6) NOT NULL,
        expiry DATETIME NOT NULL,
        used TINYINT(1) DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )");

    // Delete old OTPs for this email
    $stmt = $conn->prepare("DELETE FROM password_resets WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();

    // Insert new OTP
    $stmt = $conn->prepare("INSERT INTO password_resets (email, otp, expiry) VALUES (?, ?, ?)");
    $stmt->bind_param("sss", $email, $otp, $expiry);
    $stmt->execute();

    // Send email with OTP
    $emailSent = sendOTPEmail($email, $userName, $otp);

    if ($emailSent) {
        respond(true, 'OTP sent to your email', [
            'email' => maskEmail($email),
            'expires_in' => '10 minutes'
        ]);
    } else {
        // For development - show OTP if email fails (REMOVE IN PRODUCTION!)
        respond(true, 'OTP generated', [
            'email' => maskEmail($email),
            'otp' => $otp, // REMOVE THIS LINE IN PRODUCTION!
            'expires_in' => '10 minutes',
            'note' => 'Email service not configured - OTP shown for testing'
        ]);
    }

    $conn->close();

} catch (Exception $e) {
    error_log("Forgot Password Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}

/**
 * Send OTP via email using PHPMailer (Gmail SMTP)
 * 
 * TO ENABLE EMAIL:
 * 1. Download PHPMailer: composer require phpmailer/phpmailer
 * 2. Or download from: https://github.com/PHPMailer/PHPMailer
 * 3. Update SMTP credentials below
 */
function sendOTPEmail($email, $name, $otp)
{
    // Check if PHPMailer is available
    $phpmailerPath = __DIR__ . '/vendor/autoload.php';
    if (!file_exists($phpmailerPath)) {
        // Fallback to basic mail() function
        return sendBasicMail($email, $name, $otp);
    }

    require $phpmailerPath;

    try {
        $mail = new PHPMailer(true);

        // SMTP Configuration - UPDATE THESE!
        $mail->isSMTP();
        $mail->Host = 'smtp.gmail.com';
        $mail->SMTPAuth = true;
        $mail->Username = 'sravanireddy730@gmail.com';
        $mail->Password = 'qeerlitrbmanksms';
        $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
        $mail->Port = 587;

        // Email content
        $mail->setFrom('sravanireddy730@gmail.com', 'Digital Library');
        $mail->addAddress($email, $name);
        $mail->isHTML(true);
        $mail->Subject = 'Digital Library - Password Reset OTP';
        $mail->Body = getEmailTemplate($name, $otp);

        $mail->send();
        return true;

    } catch (Exception $e) {
        error_log("PHPMailer Error: " . $e->getMessage());
        return false;
    }
}

/**
 * Fallback: Basic PHP mail() function
 */
function sendBasicMail($email, $name, $otp)
{
    $subject = "Digital Library - Password Reset OTP";
    $message = getEmailTemplate($name, $otp);

    $headers = "MIME-Version: 1.0\r\n";
    $headers .= "Content-type: text/html; charset=UTF-8\r\n";
    $headers .= "From: Digital Library <noreply@digitallibrary.com>\r\n";

    return @mail($email, $subject, $message, $headers);
}

/**
 * Get beautiful HTML email template
 */
function getEmailTemplate($name, $otp)
{
    return "
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset='UTF-8'>
        <meta name='viewport' content='width=device-width, initial-scale=1.0'>
    </head>
    <body style='margin:0; padding:0; font-family: Arial, sans-serif; background-color: #f4f4f4;'>
        <table width='100%' cellpadding='0' cellspacing='0' style='max-width: 600px; margin: 0 auto; background-color: #ffffff;'>
            <tr>
                <td style='background: linear-gradient(135deg, #8B5CF6, #7C3AED); padding: 30px; text-align: center;'>
                    <h1 style='color: #ffffff; margin: 0; font-size: 28px;'>🔐 Password Reset</h1>
                </td>
            </tr>
            <tr>
                <td style='padding: 40px 30px;'>
                    <p style='font-size: 18px; color: #1F2937; margin-bottom: 10px;'>Hi <strong>$name</strong>,</p>
                    <p style='font-size: 16px; color: #4B5563; line-height: 1.6;'>
                        You requested to reset your password for your Digital Library account. 
                        Use the OTP below to proceed:
                    </p>
                    
                    <div style='background: linear-gradient(135deg, #8B5CF6, #7C3AED); border-radius: 12px; padding: 25px; text-align: center; margin: 30px 0;'>
                        <span style='font-size: 36px; font-weight: bold; color: #ffffff; letter-spacing: 10px;'>$otp</span>
                    </div>
                    
                    <p style='font-size: 14px; color: #6B7280; text-align: center;'>
                        ⏱️ This OTP is valid for <strong>10 minutes</strong>
                    </p>
                    
                    <hr style='border: none; border-top: 1px solid #E5E7EB; margin: 30px 0;'>
                    
                    <p style='font-size: 13px; color: #9CA3AF; text-align: center;'>
                        ⚠️ If you didn't request this, please ignore this email.
                    </p>
                </td>
            </tr>
            <tr>
                <td style='background-color: #F3F4F6; padding: 20px; text-align: center;'>
                    <p style='font-size: 12px; color: #6B7280; margin: 0;'>
                        📚 Digital Library - Your Reading Companion
                    </p>
                </td>
            </tr>
        </table>
    </body>
    </html>
    ";
}

/**
 * Mask email for privacy
 */
function maskEmail($email)
{
    $parts = explode('@', $email);
    $name = $parts[0];
    $domain = $parts[1];
    $masked = substr($name, 0, 2) . str_repeat('*', max(strlen($name) - 4, 2)) . substr($name, -2);
    return $masked . '@' . $domain;
}
?>