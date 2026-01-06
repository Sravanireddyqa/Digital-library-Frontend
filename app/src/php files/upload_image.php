<?php
/**
 * Upload Image API
 * Uploads book cover images and returns the URL
 */

// Allow from any origin
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json");

// Handle preflight
if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Only allow POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

try {
    // Check if file was uploaded
    if (!isset($_FILES['image']) || $_FILES['image']['error'] !== UPLOAD_ERR_OK) {
        $errorCode = isset($_FILES['image']) ? $_FILES['image']['error'] : 'No file';
        echo json_encode(['success' => false, 'message' => 'No file uploaded or upload error: ' . $errorCode]);
        exit();
    }

    $file = $_FILES['image'];

    // Validate file type
    $allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    $fileType = mime_content_type($file['tmp_name']);

    if (!in_array($fileType, $allowedTypes)) {
        echo json_encode(['success' => false, 'message' => 'Invalid file type: ' . $fileType]);
        exit();
    }

    // Create uploads directory if it doesn't exist
    $uploadDir = __DIR__ . '/uploads/';
    if (!file_exists($uploadDir)) {
        mkdir($uploadDir, 0755, true);
    }

    // Generate unique filename
    $extension = pathinfo($file['name'], PATHINFO_EXTENSION);
    if (empty($extension)) {
        $extension = 'jpg';
    }
    $filename = 'book_' . time() . '_' . bin2hex(random_bytes(4)) . '.' . $extension;
    $targetPath = $uploadDir . $filename;

    // Move uploaded file
    if (move_uploaded_file($file['tmp_name'], $targetPath)) {
        // Generate full URL for the image
        $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? 'https' : 'http';
        $host = $_SERVER['HTTP_HOST'];
        $baseUrl = $protocol . '://' . $host . '/digitallibrary_API/uploads/';
        $imageUrl = $baseUrl . $filename;

        echo json_encode([
            'success' => true,
            'message' => 'Image uploaded successfully',
            'cover_url' => $imageUrl,
            'filename' => $filename
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to move uploaded file']);
    }

} catch (Exception $e) {
    error_log("Upload Image Error: " . $e->getMessage());
    echo json_encode(['success' => false, 'message' => 'Server error: ' . $e->getMessage()]);
}
?>