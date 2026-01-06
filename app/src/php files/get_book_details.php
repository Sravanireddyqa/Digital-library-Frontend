<?php
/**
 * Get Book Details API
 * Returns comprehensive book information including availability and similar books
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $bookId = isset($_GET['book_id']) ? intval($_GET['book_id']) : 0;
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

    if ($bookId == 0) {
        respond(false, 'Book ID is required');
    }

    $conn = getConnection();

    // Get book details
    $sql = "SELECT * FROM books WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $bookId);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows == 0) {
        respond(false, 'Book not found');
    }

    $bookRow = $result->fetch_assoc();
    $book = [
        'id' => (int) $bookRow['id'],
        'title' => $bookRow['title'],
        'author' => $bookRow['author'],
        'category' => $bookRow['category'] ?? '',
        'description' => $bookRow['description'] ?? '',
        'isbn' => $bookRow['isbn'] ?? '',
        'publisher' => $bookRow['publisher'] ?? '',
        'published_date' => $bookRow['published_date'] ?? '',
        'pages' => (int) ($bookRow['pages'] ?? 0),
        'rating' => (float) ($bookRow['rating'] ?? 4.5),
        'cover_url' => $bookRow['cover_url'] ?? '',
        'stock' => (int) ($bookRow['stock'] ?? 10),
        'price' => (float) ($bookRow['price'] ?? 299),
        'available' => (int) ($bookRow['available'] ?? 1),
        'language' => $bookRow['language'] ?? 'EN'
    ];
    $stmt->close();

    // Get library availability (sample data if no library_books table)
    $libraries = [];
    $libQuery = "SHOW TABLES LIKE 'library_books'";
    $libCheck = $conn->query($libQuery);

    if ($libCheck->num_rows > 0) {
        $libSql = "SELECT lb.library_id, l.name, l.location as address, lb.quantity as available 
                   FROM library_books lb 
                   JOIN libraries l ON lb.library_id = l.id 
                   WHERE lb.book_id = ?";
        $libStmt = $conn->prepare($libSql);
        $libStmt->bind_param("i", $bookId);
        $libStmt->execute();
        $libResult = $libStmt->get_result();

        while ($libRow = $libResult->fetch_assoc()) {
            $libraries[] = [
                'library_id' => (int) $libRow['library_id'],
                'name' => $libRow['name'],
                'address' => $libRow['address'] ?? '',
                'available' => (int) $libRow['available']
            ];
        }
        $libStmt->close();
    }

    // If no library data, provide sample libraries
    if (empty($libraries)) {
        $libraries = [
            ['library_id' => 1, 'name' => 'SIMATS Central Library', 'address' => 'Saveetha University, Chennai', 'available' => 5],
            ['library_id' => 2, 'name' => 'Anna Centenary Library', 'address' => 'Kotturpuram, Chennai', 'available' => 3],
            ['library_id' => 3, 'name' => 'Connemara Public Library', 'address' => 'Egmore, Chennai', 'available' => 4]
        ];
    }

    // Get similar books (same category)
    $similarBooks = [];
    $category = $book['category'];
    if (!empty($category)) {
        $simSql = "SELECT id, title, author, cover_url FROM books 
                   WHERE category = ? AND id != ? 
                   ORDER BY rating DESC LIMIT 6";
        $simStmt = $conn->prepare($simSql);
        $simStmt->bind_param("si", $category, $bookId);
        $simStmt->execute();
        $simResult = $simStmt->get_result();

        while ($simRow = $simResult->fetch_assoc()) {
            $similarBooks[] = [
                'id' => (int) $simRow['id'],
                'title' => $simRow['title'],
                'author' => $simRow['author'],
                'cover_url' => $simRow['cover_url'] ?? ''
            ];
        }
        $simStmt->close();
    }

    // Check if in wishlist
    $inWishlist = false;
    if ($userId > 0) {
        $wishSql = "SELECT id FROM wishlist WHERE user_id = ? AND book_id = ?";
        $wishStmt = $conn->prepare($wishSql);
        $wishStmt->bind_param("ii", $userId, $bookId);
        $wishStmt->execute();
        $wishResult = $wishStmt->get_result();
        $inWishlist = $wishResult->num_rows > 0;
        $wishStmt->close();
    }

    respond(true, 'Book details fetched', [
        'book' => $book,
        'libraries' => $libraries,
        'similar_books' => $similarBooks,
        'in_wishlist' => $inWishlist
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("Get Book Details Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>