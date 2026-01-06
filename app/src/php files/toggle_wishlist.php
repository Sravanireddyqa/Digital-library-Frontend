<?php
/**
 * Toggle Wishlist API
 * Adds or removes a book from user's wishlist
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $data = json_decode(file_get_contents('php://input'), true);

    if (!isset($data['user_id']) || !isset($data['book_id'])) {
        respond(false, 'Missing user_id or book_id');
    }

    $userId = intval($data['user_id']);
    $bookId = intval($data['book_id']);
    $action = isset($data['action']) ? $data['action'] : 'toggle';

    $conn = getConnection();

    // Create wishlist table if not exists
    $conn->query("CREATE TABLE IF NOT EXISTS wishlist (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id INT NOT NULL,
        book_id INT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE KEY unique_wishlist (user_id, book_id)
    )");

    // Check if already in wishlist
    $check = $conn->prepare("SELECT id FROM wishlist WHERE user_id = ? AND book_id = ?");
    $check->bind_param("ii", $userId, $bookId);
    $check->execute();
    $exists = $check->get_result()->num_rows > 0;
    $check->close();

    if ($action === 'add' || ($action === 'toggle' && !$exists)) {
        // Add to wishlist
        if (!$exists) {
            $stmt = $conn->prepare("INSERT INTO wishlist (user_id, book_id) VALUES (?, ?)");
            $stmt->bind_param("ii", $userId, $bookId);
            $stmt->execute();
            $stmt->close();
            respond(true, 'Added to wishlist', ['in_wishlist' => true]);
        } else {
            respond(true, 'Already in wishlist', ['in_wishlist' => true]);
        }
    } else {
        // Remove from wishlist
        if ($exists) {
            $stmt = $conn->prepare("DELETE FROM wishlist WHERE user_id = ? AND book_id = ?");
            $stmt->bind_param("ii", $userId, $bookId);
            $stmt->execute();
            $stmt->close();
            respond(true, 'Removed from wishlist', ['in_wishlist' => false]);
        } else {
            respond(true, 'Not in wishlist', ['in_wishlist' => false]);
        }
    }

    $conn->close();

} catch (Exception $e) {
    error_log("Toggle Wishlist Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>