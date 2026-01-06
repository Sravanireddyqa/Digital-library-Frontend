<?php
/**
 * Database Configuration for Digital Library
 * Update these values to match your MySQL server settings
 */

define('DB_HOST', 'localhost');
define('DB_USER', 'root');           // Your MySQL username
define('DB_PASS', '');               // Your MySQL password
define('DB_NAME', 'digitallibrary'); // Database name

// Create database connection
function getConnection() {
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
    
    if ($conn->connect_error) {
        die(json_encode([
            'success' => false,
            'message' => 'Database connection failed: ' . $conn->connect_error
        ]));
    }
    
    $conn->set_charset("utf8mb4");
    return $conn;
}

// Set headers for JSON API response
function setApiHeaders() {
    header('Content-Type: application/json');
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, Authorization');
    
    // Handle preflight requests
    if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
        http_response_code(200);
        exit();
    }
}

// Get JSON input from request body
function getJsonInput() {
    $json = file_get_contents('php://input');
    return json_decode($json, true);
}

// Send JSON response
function sendResponse($success, $message, $data = null) {
    $response = [
        'success' => $success,
        'message' => $message
    ];
    
    if ($data !== null) {
        $response = array_merge($response, $data);
    }
    
    echo json_encode($response);
    exit();
}

// Validate email format
function isValidEmail($email) {
    return filter_var($email, FILTER_VALIDATE_EMAIL) !== false;
}

// Sanitize input
function sanitize($conn, $input) {
    return $conn->real_escape_string(trim($input));
}
?>
