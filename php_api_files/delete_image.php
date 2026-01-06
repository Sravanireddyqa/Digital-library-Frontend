<?php
/**
 * Delete Image API
 * ================
 * Handles deleting uploaded images
 * 
 * Parameters (POST JSON):
 * - filename: The filename to delete
 * - type: 'book_cover' or 'profile'
 * 
 * Returns:
 * - success: true/false
 * - message: Status message
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, DELETE');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'db.php';

// Get input
$input = json_decode(file_get_contents('php://input'), true);

if (!$input) {
    // Try POST parameters
    $input = $_POST;
}

$filename = isset($input['filename']) ? $input['filename'] : '';
$type = isset($input['type']) ? $input['type'] : 'book_cover';

if (empty($filename)) {
    echo json_encode(['success' => false, 'message' => 'Filename is required']);
    exit;
}

// Sanitize filename to prevent directory traversal
$filename = basename($filename);

// Determine directory
$upload_dir = __DIR__ . '/uploads/';
$target_dir = ($type === 'profile') ? $upload_dir . 'profiles/' : $upload_dir . 'book_covers/';
$file_path = $target_dir . $filename;

// Check if file exists
if (!file_exists($file_path)) {
    echo json_encode(['success' => false, 'message' => 'File not found']);
    exit;
}

// Delete the file
if (unlink($file_path)) {
    echo json_encode([
        'success' => true,
        'message' => 'Image deleted successfully'
    ]);
} else {
    echo json_encode(['success' => false, 'message' => 'Failed to delete image']);
}
?>