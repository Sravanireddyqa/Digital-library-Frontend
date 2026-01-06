<?php
/**
 * Login API Endpoint
 * POST /api/login.php
 * 
 * Request body:
 * {
 *   "email": "user@email.com",
 *   "password": "password123"
 * }
 */

require_once '../config/db.php';

setApiHeaders();

// Only allow POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    sendResponse(false, 'Method not allowed. Use POST.');
}

// Get input
$input = getJsonInput();

if (!$input) {
    http_response_code(400);
    sendResponse(false, 'Invalid JSON input');
}

// Validate required fields
if (empty($input['email']) || empty($input['password'])) {
    http_response_code(400);
    sendResponse(false, 'Email and password are required');
}

$email = trim($input['email']);
$password = $input['password'];

// Validate email format
if (!isValidEmail($email)) {
    http_response_code(400);
    sendResponse(false, 'Invalid email format');
}

// Connect to database
$conn = getConnection();

// Sanitize email
$email = sanitize($conn, $email);

// Check if user exists
$stmt = $conn->prepare("SELECT id, name, email, password, role, is_active, created_at FROM users WHERE email = ? LIMIT 1");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    http_response_code(401);
    sendResponse(false, 'Invalid email or password');
}

$user = $result->fetch_assoc();

// Check if account is active
if (!$user['is_active']) {
    http_response_code(403);
    sendResponse(false, 'Your account has been deactivated. Please contact support.');
}

// Verify password
if (!password_verify($password, $user['password'])) {
    http_response_code(401);
    sendResponse(false, 'Invalid email or password');
}

// Update last login time
$updateStmt = $conn->prepare("UPDATE users SET last_login = NOW() WHERE id = ?");
$updateStmt->bind_param("i", $user['id']);
$updateStmt->execute();

// Remove password from response
unset($user['password']);

// Send success response
http_response_code(200);
sendResponse(true, 'Login successful', [
    'user' => [
        'id' => (int) $user['id'],
        'name' => $user['name'],
        'email' => $user['email'],
        'role' => $user['role'],
        'created_at' => $user['created_at']
    ]
]);

$stmt->close();
$conn->close();
?>