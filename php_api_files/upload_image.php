<?php
/**
 * Image Upload API
 * ================
 * Handles uploading book cover images and profile pictures
 * 
 * Parameters:
 * - image: The image file (multipart/form-data)
 * - type: 'book_cover' or 'profile' (optional, defaults to 'book_cover')
 * - id: book_id or user_id (optional, for linking)
 * 
 * Returns:
 * - success: true/false
 * - image_url: Full URL to access the uploaded image
 * - filename: The saved filename
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'db.php';

// Create uploads directory if it doesn't exist
$upload_dir = __DIR__ . '/uploads/';
$book_covers_dir = $upload_dir . 'book_covers/';
$profiles_dir = $upload_dir . 'profiles/';

if (!file_exists($upload_dir)) {
    mkdir($upload_dir, 0777, true);
}
if (!file_exists($book_covers_dir)) {
    mkdir($book_covers_dir, 0777, true);
}
if (!file_exists($profiles_dir)) {
    mkdir($profiles_dir, 0777, true);
}

// Only accept POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Only POST method allowed']);
    exit;
}

// Check if image was uploaded
if (!isset($_FILES['image']) || $_FILES['image']['error'] !== UPLOAD_ERR_OK) {
    $error_messages = [
        UPLOAD_ERR_INI_SIZE => 'File too large (server limit)',
        UPLOAD_ERR_FORM_SIZE => 'File too large (form limit)',
        UPLOAD_ERR_PARTIAL => 'File partially uploaded',
        UPLOAD_ERR_NO_FILE => 'No file uploaded',
        UPLOAD_ERR_NO_TMP_DIR => 'Missing temp folder',
        UPLOAD_ERR_CANT_WRITE => 'Failed to write file',
        UPLOAD_ERR_EXTENSION => 'Upload blocked by extension'
    ];
    $error_code = isset($_FILES['image']) ? $_FILES['image']['error'] : UPLOAD_ERR_NO_FILE;
    $message = isset($error_messages[$error_code]) ? $error_messages[$error_code] : 'Unknown upload error';
    echo json_encode(['success' => false, 'message' => $message]);
    exit;
}

$file = $_FILES['image'];
$type = isset($_POST['type']) ? $_POST['type'] : 'book_cover';
$id = isset($_POST['id']) ? intval($_POST['id']) : 0;

// Validate file type
$allowed_types = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
$finfo = finfo_open(FILEINFO_MIME_TYPE);
$mime_type = finfo_file($finfo, $file['tmp_name']);
finfo_close($finfo);

if (!in_array($mime_type, $allowed_types)) {
    echo json_encode(['success' => false, 'message' => 'Invalid file type. Allowed: JPG, PNG, GIF, WebP']);
    exit;
}

// Validate file size (max 5MB)
$max_size = 5 * 1024 * 1024; // 5MB
if ($file['size'] > $max_size) {
    echo json_encode(['success' => false, 'message' => 'File too large. Maximum size is 5MB']);
    exit;
}

// Determine target directory
$target_dir = ($type === 'profile') ? $profiles_dir : $book_covers_dir;
$subfolder = ($type === 'profile') ? 'profiles/' : 'book_covers/';

// Generate unique filename
$extension = pathinfo($file['name'], PATHINFO_EXTENSION);
$filename = $type . '_' . time() . '_' . uniqid() . '.' . strtolower($extension);
$target_path = $target_dir . $filename;

// Move uploaded file
if (move_uploaded_file($file['tmp_name'], $target_path)) {
    // Build the full URL
    $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? 'https' : 'http';
    $host = $_SERVER['HTTP_HOST'];
    $script_dir = dirname($_SERVER['SCRIPT_NAME']);
    $image_url = $protocol . '://' . $host . $script_dir . '/uploads/' . $subfolder . $filename;

    // If id is provided, update the database
    if ($id > 0) {
        try {
            if ($type === 'book_cover') {
                $stmt = $pdo->prepare("UPDATE books SET cover_url = ? WHERE id = ?");
                $stmt->execute([$image_url, $id]);
            } elseif ($type === 'profile') {
                $stmt = $pdo->prepare("UPDATE users SET profile_image = ? WHERE id = ?");
                $stmt->execute([$image_url, $id]);
            }
        } catch (PDOException $e) {
            // Log error but don't fail - image is still uploaded
            error_log("Database update failed: " . $e->getMessage());
        }
    }

    echo json_encode([
        'success' => true,
        'message' => 'Image uploaded successfully',
        'image_url' => $image_url,
        'filename' => $filename
    ]);
} else {
    echo json_encode(['success' => false, 'message' => 'Failed to save image']);
}
?>