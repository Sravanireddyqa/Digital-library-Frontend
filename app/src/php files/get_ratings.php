<?php
/**
 * Get Ratings API
 * Returns all ratings for admin to view, or ratings for a specific book
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $conn = getConnection();

    // Check if ratings table exists
    $tableCheck = $conn->query("SHOW TABLES LIKE 'ratings'");
    if ($tableCheck->num_rows == 0) {
        respond(true, 'No ratings yet', ['ratings' => [], 'count' => 0]);
    }

    // Optional book_id filter
    $bookId = isset($_GET['book_id']) ? intval($_GET['book_id']) : null;

    $sql = "
        SELECT r.id, r.rating, r.review, r.created_at,
               u.id as user_id, u.name as user_name, u.email as user_email,
               b.id as book_id, b.title as book_title, b.author as book_author
        FROM ratings r
        LEFT JOIN users u ON r.user_id = u.id
        LEFT JOIN books b ON r.book_id = b.id
    ";

    if ($bookId) {
        $sql .= " WHERE r.book_id = " . $bookId;
    }

    $sql .= " ORDER BY r.created_at DESC";

    $result = $conn->query($sql);

    $ratings = [];
    while ($row = $result->fetch_assoc()) {
        $ratings[] = [
            'id' => (int) $row['id'],
            'rating' => (int) $row['rating'],
            'review' => $row['review'],
            'created_at' => $row['created_at'],
            'user_id' => (int) $row['user_id'],
            'user_name' => $row['user_name'],
            'user_email' => $row['user_email'],
            'book_id' => (int) $row['book_id'],
            'book_title' => $row['book_title'],
            'book_author' => $row['book_author']
        ];
    }

    // Calculate stats
    $statsQuery = $conn->query("
        SELECT AVG(rating) as avg_rating, COUNT(*) as total_ratings
        FROM ratings
    ");
    $stats = $statsQuery->fetch_assoc();

    respond(true, 'Ratings fetched successfully', [
        'ratings' => $ratings,
        'count' => count($ratings),
        'average_rating' => round($stats['avg_rating'], 1),
        'total_ratings' => (int) $stats['total_ratings']
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("Get Ratings Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>