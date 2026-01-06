<?php
/**
 * Register API Endpoint
 * POST /api/register.php
 * 
 * Request body for Reader:
 * {
 *   "name": "John Doe",
 *   "email": "user@email.com",
 *   "password": "password123",
 *   "role": "reader"
 * }
 * 
 * Request body for Admin:
 * {
 *   "name": "Library Name",
 *   "email": "admin@email.com",
 *   "password": "password123",
 *   "role": "admin",
 *   "library_name": "City Central Library",
 *   "library_location": "123 Main Street, City"
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
if (empty($input['name']) || empty($input['email']) || empty($input['password'])) {
    http_response_code(400);
    sendResponse(false, 'Name, email, and password are required');
}

$name = trim($input['name']);
$email = trim($input['email']);
$password = $input['password'];
$role = isset($input['role']) ? trim($input['role']) : 'reader';

// Admin-specific fields
$libraryName = isset($input['library_name']) ? trim($input['library_name']) : null;
$libraryLocation = isset($input['library_location']) ? trim($input['library_location']) : null;

// Validate name length
if (strlen($name) < 2 || strlen($name) > 100) {
    http_response_code(400);
    sendResponse(false, 'Name must be between 2 and 100 characters');
}

// Validate email format
if (!isValidEmail($email)) {
    http_response_code(400);
    sendResponse(false, 'Invalid email format');
}

// Validate password strength
if (strlen($password) < 6) {
    http_response_code(400);
    sendResponse(false, 'Password must be at least 6 characters');
}

// Validate role
$allowedRoles = ['reader', 'admin'];
if (!in_array($role, $allowedRoles)) {
    $role = 'reader';  // Default to reader if invalid role
}

// Validate admin-specific fields
if ($role === 'admin') {
    if (empty($libraryName)) {
        http_response_code(400);
        sendResponse(false, 'Library name is required for admin registration');
    }
    if (empty($libraryLocation)) {
        http_response_code(400);
        sendResponse(false, 'Library location is required for admin registration');
    }
}

// Connect to database
$conn = getConnection();

// Sanitize inputs
$name = sanitize($conn, $name);
$email = sanitize($conn, $email);
if ($libraryName)
    $libraryName = sanitize($conn, $libraryName);
if ($libraryLocation)
    $libraryLocation = sanitize($conn, $libraryLocation);

// Check if email already exists
$checkStmt = $conn->prepare("SELECT id FROM users WHERE email = ? LIMIT 1");
$checkStmt->bind_param("s", $email);
$checkStmt->execute();
$checkResult = $checkStmt->get_result();

if ($checkResult->num_rows > 0) {
    http_response_code(409);
    sendResponse(false, 'An account with this email already exists');
}

// Hash the password
$hashedPassword = password_hash($password, PASSWORD_DEFAULT);

// Insert new user with library fields
$insertStmt = $conn->prepare("INSERT INTO users (name, email, password, role, library_name, library_location, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, 1, NOW())");
$insertStmt->bind_param("ssssss", $name, $email, $hashedPassword, $role, $libraryName, $libraryLocation);

if ($insertStmt->execute()) {
    $userId = $conn->insert_id;

    $responseData = [
        'user' => [
            'id' => $userId,
            'name' => $name,
            'email' => $email,
            'role' => $role
        ]
    ];

    // Add library info for admin
    if ($role === 'admin') {
        $responseData['user']['library_name'] = $libraryName;
        $responseData['user']['library_location'] = $libraryLocation;
    }

    http_response_code(201);
    sendResponse(true, 'Registration successful! You can now login.', $responseData);
} else {
    http_response_code(500);
    sendResponse(false, 'Registration failed. Please try again.');
}

$checkStmt->close();
$insertStmt->close();
$conn->close();
?>