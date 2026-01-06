<?php
/**
 * Get Wishlist API
 * Returns all books in user's wishlist
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

    if ($userId == 0) {
        respond(false, 'User ID is required');
    }

    $conn = getConnection();

    // Create wishlist table if not exists
    $conn->query("CREATE TABLE IF NOT EXISTS wishlist (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id INT NOT NULL,
        book_id INT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE KEY unique_wishlist (user_id, book_id)
    )");

    // Get wishlist with book details
    $sql = "SELECT w.id as wishlist_id, w.created_at as added_at,
                   b.id, b.title, b.author, b.category, b.cover_url, 
                   b.rating, b.price, b.description
            FROM wishlist w
            JOIN books b ON w.book_id = b.id
            WHERE w.user_id = ?
            ORDER BY w.created_at DESC";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $result = $stmt->get_result();

    $books = [];
    while ($row = $result->fetch_assoc()) {
        $books[] = [
            'id' => (int) $row['id'],
            'wishlist_id' => (int) $row['wishlist_id'],
            'title' => $row['title'],
            'author' => $row['author'],
            'category' => $row['category'] ?? '',
            'cover_url' => $row['cover_url'] ?? '',
            'rating' => (float) ($row['rating'] ?? 4.5),
            'price' => (float) ($row['price'] ?? 0),
            'description' => $row['description'] ?? '',
            'added_at' => $row['added_at']
        ];
    }

    $stmt->close();
    $conn->close();

    respond(true, 'Wishlist fetched', [
        'books' => $books,
        'count' => count($books)
    ]);

} catch (Exception $e) {
    error_log("Get Wishlist Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>