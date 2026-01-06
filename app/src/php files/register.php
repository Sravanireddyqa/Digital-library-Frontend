<?php
/**
 * Register API - Updated for existing database schema
 * POST /register.php
 * Body: { "name": "...", "email": "...", "password": "...", "role": "reader|admin" }
 * For admin: also include "library_name" and "library_location"
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

if (!$input || empty($input['name']) || empty($input['email']) || empty($input['password'])) {
    http_response_code(400);
    respond(false, 'Name, email, and password required');
}

$name = trim($input['name']);
$email = trim($input['email']);
$password = $input['password'];
$role = isset($input['role']) ? trim($input['role']) : 'reader';
$libraryName = isset($input['library_name']) ? trim($input['library_name']) : null;
$libraryLocation = isset($input['library_location']) ? trim($input['library_location']) : null;

// Validate
if (strlen($name) < 2) {
    respond(false, 'Name too short');
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    respond(false, 'Invalid email');
}

if (strlen($password) < 6) {
    respond(false, 'Password must be at least 6 characters');
}

// Validate role
if (!in_array($role, ['reader', 'admin'])) {
    $role = 'reader';
}

// Admin validation
if ($role === 'admin') {
    if (empty($libraryName) || empty($libraryLocation)) {
        respond(false, 'Library name and location required for admin');
    }
}

// Connect
$conn = getConnection();

// Check if email exists
$check = $conn->prepare("SELECT id FROM users WHERE email = ?");
$check->bind_param("s", $email);
$check->execute();
if ($check->get_result()->num_rows > 0) {
    http_response_code(409);
    respond(false, 'Email already registered');
}

// Hash password
$hashedPassword = password_hash($password, PASSWORD_DEFAULT);

// Insert user - using your database columns: user_type (not role), status (not is_active)
$stmt = $conn->prepare("INSERT INTO users (name, email, password, user_type, library_name, library_location, status, created_at) VALUES (?, ?, ?, ?, ?, ?, 'active', NOW())");
$stmt->bind_param("ssssss", $name, $email, $hashedPassword, $role, $libraryName, $libraryLocation);

if ($stmt->execute()) {
    $userId = $conn->insert_id;

    $userData = [
        'id' => $userId,
        'name' => $name,
        'email' => $email,
        'role' => $role
    ];

    if ($role === 'admin') {
        $userData['library_name'] = $libraryName;
        $userData['library_location'] = $libraryLocation;
    }

    // Notify all admins about new user registration (only for reader registrations)
    if ($role === 'reader') {
        require_once 'notification_helper.php';
        notifyNewUserRegistration($userId, $name, $email, $conn);
    }

    http_response_code(201);
    respond(true, 'Registration successful', ['user' => $userData]);
} else {
    http_response_code(500);
    respond(false, 'Registration failed: ' . $conn->error);
}

$check->close();
$stmt->close();
$conn->close();
?>