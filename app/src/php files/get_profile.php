<?php
/**
 * Get User Profile API
 * Returns user profile data based on user_id
 */

require_once 'db.php';

// Set headers
setHeaders();

// Only allow GET requests
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

// Get user_id from query parameter
$userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

if ($userId <= 0) {
    respond(false, 'Invalid user ID');
}

try {
    // Get database connection
    $conn = getConnection();

    // Fetch user data
    $stmt = $conn->prepare("SELECT id, name, email, user_type, library_name, library_location, status, created_at FROM users WHERE id = ?");
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        http_response_code(404);
        respond(false, 'User not found');
    }

    $user = $result->fetch_assoc();

    // Check if user is active
    if ($user['status'] !== 'active') {
        respond(false, 'Account is inactive');
    }

    // Return user data (excluding sensitive info)
    $userData = [
        'id' => $user['id'],
        'name' => $user['name'],
        'email' => $user['email'],
        'role' => $user['user_type'],
        'library_name' => $user['library_name'] ?? '',
        'library_location' => $user['library_location'] ?? '',
        'created_at' => $user['created_at']
    ];

    respond(true, 'Profile fetched successfully', ['user' => $userData]);

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    error_log("Get Profile Error: " . $e->getMessage());
    http_response_code(500);
    respond(false, 'Server error');
}
?>