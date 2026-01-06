<?php
/**
 * Delete Book API
 * Deletes a book by ID
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

try {
    // Get database connection
    $conn = getConnection();

    // Check if book exists
    $checkStmt = $conn->prepare("SELECT id FROM books WHERE id = ?");
    $checkStmt->bind_param("i", $bookId);
    $checkStmt->execute();
    $checkResult = $checkStmt->get_result();

    if ($checkResult->num_rows === 0) {
        respond(false, 'Book not found');
    }
    $checkStmt->close();

    // Delete book
    $stmt = $conn->prepare("DELETE FROM books WHERE id = ?");
    $stmt->bind_param("i", $bookId);

    if ($stmt->execute()) {
        respond(true, 'Book deleted successfully');
    } else {
        respond(false, 'Failed to delete book');
    }

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    error_log("Delete Book Error: " . $e->getMessage());
    http_response_code(500);
    respond(false, 'Server error');
}
?>