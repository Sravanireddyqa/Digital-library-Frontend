<?php
/**
 * Login API - Updated for existing database schema
 * POST /login.php
 * Body: { "email": "...", "password": "..." }
 */

require_once 'db.php';
setHeaders();

// POST only
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    respond(false, 'Use POST method');
}

// Get input
$input = getInput();

if (!$input || empty($input['email']) || empty($input['password'])) {
    http_response_code(400);
    respond(false, 'Email and password required');
}

$email = trim($input['email']);
$password = $input['password'];

// Connect
$conn = getConnection();

// Find user - using your database columns: user_type, status
$stmt = $conn->prepare("SELECT id, name, email, password, user_type, library_name, library_location, status FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    respond(false, 'Invalid email or password');
}

$user = $result->fetch_assoc();

// Check if blocked
if ($user['status'] === 'blocked') {
    respond(false, 'Account blocked');
}

// Verify password - check both hashed and plain text for backward compatibility
$passwordValid = false;
if (password_verify($password, $user['password'])) {
    $passwordValid = true;
} elseif ($password === $user['password']) {
    // Plain text password (legacy accounts)
    $passwordValid = true;
}

if (!$passwordValid) {
    respond(false, 'Invalid email or password');
}

// Success response
$userData = [
    'id' => $user['id'],
    'name' => $user['name'],
    'email' => $user['email'],
    'role' => $user['user_type']  // Map user_type to role for Android
];

if ($user['user_type'] === 'admin') {
    $userData['library_name'] = $user['library_name'];
    $userData['library_location'] = $user['library_location'];
}

respond(true, 'Login successful', ['user' => $userData]);

$stmt->close();
$conn->close();
?>