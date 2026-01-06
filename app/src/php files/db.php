<?php
/**
 * Database Configuration
 * ======================
 * Copy this file to: C:\xampp\htdocs\digitallibrary_API\db.php
 */

// Database settings - UPDATE THESE
define('DB_HOST', 'localhost');
define('DB_USER', 'root');
define('DB_PASS', '');
define('DB_NAME', 'digitallibrary');

/**
 * Get database connection
 */
function getConnection()
{
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);

    if ($conn->connect_error) {
        http_response_code(500);
        die(json_encode(['success' => false, 'message' => 'DB Error: ' . $conn->connect_error]));
    }

    $conn->set_charset("utf8mb4");
    return $conn;
}

/**
 * Set headers for API response
 */
function setHeaders()
{
    header('Content-Type: application/json');
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type');

    if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
        exit(0);
    }
}

/**
 * Get JSON input
 */
function getInput()
{
    return json_decode(file_get_contents('php://input'), true);
}

/**
 * Send JSON response
 */
function respond($success, $message, $data = [])
{
    echo json_encode(array_merge(['success' => $success, 'message' => $message], $data));
    exit;
}
?>