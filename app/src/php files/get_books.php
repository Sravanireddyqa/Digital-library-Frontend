<?php
/**
 * Get Books API
 * Returns list of all books
 */

require_once 'db.php';

// Set headers
setHeaders();

// Only allow GET requests
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

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
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )";

        if (!$conn->query($createTableSQL)) {
            respond(false, 'Failed to create books table');
        }
    } else {
        // Check if rating column exists, add if not
        $columnCheck = $conn->query("SHOW COLUMNS FROM books LIKE 'rating'");
        if ($columnCheck->num_rows == 0) {
            $conn->query("ALTER TABLE books ADD COLUMN rating DECIMAL(3,2) DEFAULT 0");
        }
        // Check if cover_url/image column exists, add if not
        $imageCheck = $conn->query("SHOW COLUMNS FROM books LIKE 'cover_url'");
        if ($imageCheck->num_rows == 0) {
            $conn->query("ALTER TABLE books ADD COLUMN cover_url VARCHAR(500)");
        }
        // Check if is_new column exists, add if not
        $isNewCheck = $conn->query("SHOW COLUMNS FROM books LIKE 'is_new'");
        if ($isNewCheck->num_rows == 0) {
            $conn->query("ALTER TABLE books ADD COLUMN is_new TINYINT(1) DEFAULT 0");
        }
    }

    // Optional category filter
    $category = isset($_GET['category']) ? trim($_GET['category']) : null;

    // Build query - include cover_url and is_new
    $sql = "SELECT id, title, author, isbn, category, price, stock, publisher, published_date, pages, description, IFNULL(rating, 0) as rating, cover_url, IFNULL(is_new, 0) as is_new, created_at FROM books";

    if ($category && $category !== 'All Categories') {
        $sql .= " WHERE category = ?";
    }

    $sql .= " ORDER BY created_at DESC";

    if ($category && $category !== 'All Categories') {
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("s", $category);
        $stmt->execute();
        $result = $stmt->get_result();
    } else {
        $result = $conn->query($sql);
    }

    if (!$result) {
        respond(false, 'Query failed: ' . $conn->error);
    }

    $books = [];

    // Get the base URL dynamically
    $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
    $host = $_SERVER['HTTP_HOST'];
    $basePath = str_replace('\\', '/', dirname($_SERVER['SCRIPT_NAME'])); // Fix Windows backslashes
    $baseUrl = $protocol . '://' . $host . $basePath . '/';

    while ($row = $result->fetch_assoc()) {
        // Construct full cover URL if it's a relative path
        $coverUrl = $row['cover_url'] ?? '';
        if (!empty($coverUrl) && !preg_match('/^https?:\/\//', $coverUrl)) {
            // It's a relative path, prepend base URL
            $coverUrl = $baseUrl . $coverUrl;
        } elseif (!empty($coverUrl) && preg_match('/^https?:\/\//', $coverUrl)) {
            // It's an absolute URL, extract relative path and rebuild with current host
            if (preg_match('/uploads\/[^\/]+\.(jpg|jpeg|png|gif|webp)$/i', $coverUrl, $matches)) {
                $coverUrl = $baseUrl . $matches[0];
            }
        }

        $books[] = [
            'id' => (int) $row['id'],
            'title' => $row['title'],
            'author' => $row['author'],
            'isbn' => $row['isbn'],
            'category' => $row['category'],
            'price' => (float) $row['price'],
            'stock' => (int) $row['stock'],
            'publisher' => $row['publisher'],
            'published_date' => $row['published_date'],
            'pages' => (int) $row['pages'],
            'description' => $row['description'],
            'rating' => (float) $row['rating'],
            'cover_url' => $coverUrl,
            'is_new' => (int) ($row['is_new'] ?? 0),
            'created_at' => $row['created_at']
        ];
    }

    respond(true, 'Books fetched successfully', ['books' => $books, 'count' => count($books)]);

    $conn->close();

} catch (Exception $e) {
    error_log("Get Books Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>