<?php
/**
 * Update Book API
 * Updates an existing book
 */

require_once 'db.php';

// Set headers
setHeaders();

// Only allow POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

// Get JSON input
$input = getInput();

// Validate book_id
if (empty($input['book_id'])) {
    respond(false, 'Book ID is required');
}

$bookId = intval($input['book_id']);
$title = trim($input['title'] ?? '');
$author = trim($input['author'] ?? '');
$isbn = trim($input['isbn'] ?? '');
$category = trim($input['category'] ?? '');
$price = floatval($input['price'] ?? 0);
$stock = intval($input['stock'] ?? 0);
$publisher = trim($input['publisher'] ?? '');
$published_date = trim($input['published_date'] ?? '');
$pages = intval($input['pages'] ?? 0);
$description = trim($input['description'] ?? '');
$cover_url = trim($input['cover_url'] ?? '');  // Add cover_url support

if (empty($title)) {
    respond(false, 'Book title is required');
}

try {
    $conn = getConnection();

    // Check if book exists
    $checkStmt = $conn->prepare("SELECT id FROM books WHERE id = ?");
    $checkStmt->bind_param("i", $bookId);
    $checkStmt->execute();
    if ($checkStmt->get_result()->num_rows === 0) {
        respond(false, 'Book not found');
    }
    $checkStmt->close();

    // Update book (including cover_url)
    $stmt = $conn->prepare("UPDATE books SET title=?, author=?, isbn=?, category=?, price=?, stock=?, publisher=?, published_date=?, pages=?, description=?, cover_url=? WHERE id=?");
    $stmt->bind_param("ssssdiisissi", $title, $author, $isbn, $category, $price, $stock, $publisher, $published_date, $pages, $description, $cover_url, $bookId);

    if ($stmt->execute()) {
        respond(true, 'Book updated successfully');
    } else {
        respond(false, 'Failed to update book: ' . $stmt->error);
    }

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    error_log("Update Book Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>