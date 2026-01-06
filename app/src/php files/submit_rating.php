<?php
/**
 * Submit Rating API
 * Allows users to rate books after returning them
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $data = json_decode(file_get_contents('php://input'), true);

    if (!isset($data['user_id']) || !isset($data['book_id']) || !isset($data['rating'])) {
        respond(false, 'Missing user_id, book_id, or rating');
    }

    $userId = intval($data['user_id']);
    $bookId = intval($data['book_id']);
    $rating = intval($data['rating']);
    $review = isset($data['review']) ? trim($data['review']) : '';

    // Validate rating (1-5)
    if ($rating < 1 || $rating > 5) {
        respond(false, 'Rating must be between 1 and 5');
    }

    $conn = getConnection();

    // Check if ratings table exists, create if not
    $conn->query("CREATE TABLE IF NOT EXISTS ratings (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id INT NOT NULL,
        book_id INT NOT NULL,
        rating INT NOT NULL,
        review TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE KEY unique_user_book (user_id, book_id)
    )");

    // Check if user has a returned reservation for this book
    $checkReturned = $conn->prepare("
        SELECT id FROM reservations 
        WHERE user_id = ? AND book_id = ? AND LOWER(status) = 'returned'
        LIMIT 1
    ");
    $checkReturned->bind_param("ii", $userId, $bookId);
    $checkReturned->execute();
    $returnedResult = $checkReturned->get_result();

    if ($returnedResult->num_rows === 0) {
        $checkReturned->close();
        respond(false, 'You can only rate books you have returned');
    }
    $checkReturned->close();

    // Check if user has already rated this book
    $checkRating = $conn->prepare("SELECT id, rating FROM ratings WHERE user_id = ? AND book_id = ?");
    $checkRating->bind_param("ii", $userId, $bookId);
    $checkRating->execute();
    $existingRating = $checkRating->get_result()->fetch_assoc();
    $checkRating->close();

    if ($existingRating) {
        respond(false, 'You have already rated this book', [
            'already_rated' => true,
            'your_rating' => $existingRating['rating']
        ]);
    }

    // Insert new rating
    $stmt = $conn->prepare("INSERT INTO ratings (user_id, book_id, rating, review) VALUES (?, ?, ?, ?)");
    $stmt->bind_param("iiis", $userId, $bookId, $rating, $review);

    if ($stmt->execute()) {
        $stmt->close();

        // Update book's average rating
        $avgQuery = $conn->prepare("
            SELECT AVG(rating) as avg_rating, COUNT(*) as count 
            FROM ratings WHERE book_id = ?
        ");
        $avgQuery->bind_param("i", $bookId);
        $avgQuery->execute();
        $avgResult = $avgQuery->get_result()->fetch_assoc();
        $avgRating = round($avgResult['avg_rating'], 1);
        $ratingCount = $avgResult['count'];
        $avgQuery->close();

        // Update book's rating
        $updateBook = $conn->prepare("UPDATE books SET rating = ? WHERE id = ?");
        $updateBook->bind_param("di", $avgRating, $bookId);
        $updateBook->execute();
        $updateBook->close();

        respond(true, 'Rating submitted successfully', [
            'average_rating' => $avgRating,
            'total_ratings' => $ratingCount
        ]);
    } else {
        respond(false, 'Failed to submit rating');
    }

    $conn->close();

} catch (Exception $e) {
    error_log("Submit Rating Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>