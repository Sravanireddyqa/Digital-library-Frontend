<?php
/**
 * Add Book API
 * Adds a new book to the database
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

// Validate required fields
if (empty($input['title'])) {
    respond(false, 'Book title is required');
}
if (empty($input['author'])) {
    respond(false, 'Author is required');
}

// Get input values
$title = trim($input['title']);
$author = trim($input['author']);
$isbn = trim($input['isbn'] ?? '');
$category = trim($input['category'] ?? 'Uncategorized');
$price = floatval($input['price'] ?? 0);
$stock = intval($input['stock'] ?? 0);
$publisher = trim($input['publisher'] ?? '');
$published_date = trim($input['published_date'] ?? '');
$pages = intval($input['pages'] ?? 0);
$description = trim($input['description'] ?? '');
$cover_url = trim($input['cover_url'] ?? '');  // Add cover_url support

try {
    // Get database connection
    $conn = getConnection();

    // Check if books table exists, create if not
    $tableCheck = $conn->query("SHOW TABLES LIKE 'books'");
    if ($tableCheck->num_rows == 0) {
        // Create books table
        $createTableSQL = "CREATE TABLE books (
            id INT AUTO_INCREMENT PRIMARY KEY,
            title VARCHAR(255) NOT NULL,
            author VARCHAR(255),
            isbn VARCHAR(50),
            category VARCHAR(100),
            price DECIMAL(10,2) DEFAULT 0,
            stock INT DEFAULT 0,
            publisher VARCHAR(255),
            published_date VARCHAR(50),
            pages INT DEFAULT 0,
            description TEXT,
            rating DECIMAL(3,2) DEFAULT 0,
            cover_url VARCHAR(500),
            is_new TINYINT(1) DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )";

        if (!$conn->query($createTableSQL)) {
            respond(false, 'Failed to create books table: ' . $conn->error);
        }
    } else {
        // Check if is_new column exists, add it if not
        $columnCheck = $conn->query("SHOW COLUMNS FROM books LIKE 'is_new'");
        if ($columnCheck->num_rows == 0) {
            $conn->query("ALTER TABLE books ADD COLUMN is_new TINYINT(1) DEFAULT 0");
        }
    }

    // Check if ISBN already exists (if provided)
    if (!empty($isbn)) {
        $checkStmt = $conn->prepare("SELECT id FROM books WHERE isbn = ?");
        $checkStmt->bind_param("s", $isbn);
        $checkStmt->execute();
        $checkResult = $checkStmt->get_result();
        if ($checkResult->num_rows > 0) {
            respond(false, 'A book with this ISBN already exists');
        }
        $checkStmt->close();
    }

    // Insert book with is_new = 1 (new book added via app)
    $isNew = 1;
    $stmt = $conn->prepare("INSERT INTO books (title, author, isbn, category, price, stock, publisher, published_date, pages, description, cover_url, is_new, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())");
    $stmt->bind_param("ssssdississi", $title, $author, $isbn, $category, $price, $stock, $publisher, $published_date, $pages, $description, $cover_url, $isNew);

    if ($stmt->execute()) {
        $bookId = $stmt->insert_id;

        // Broadcast notification to all users about new book
        require_once 'notification_helper.php';
        notifyNewBookAdded($bookId, $title, $author, $conn);

        respond(true, 'Book added successfully', ['book_id' => $bookId]);
    } else {
        respond(false, 'Failed to add book: ' . $stmt->error);
    }

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    error_log("Add Book Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>