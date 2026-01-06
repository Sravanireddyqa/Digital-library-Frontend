<?php
/**
 * Get Category Counts API
 * Returns count of books per category dynamically from database
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $conn = getConnection();

    // Get all categories with exact counts
    $sql = "SELECT category, COUNT(*) as count FROM books 
            WHERE category IS NOT NULL AND category != '' 
            GROUP BY category
            ORDER BY count DESC";

    $result = $conn->query($sql);

    if (!$result) {
        respond(false, 'Query failed: ' . $conn->error);
    }

    $categories = [];
    $categoryList = [];

    while ($row = $result->fetch_assoc()) {
        $catName = $row['category'];
        $catCount = (int) $row['count'];
        $categories[$catName] = $catCount;
        $categoryList[] = [
            'name' => $catName,
            'count' => $catCount
        ];
    }

    // Get total books
    $totalResult = $conn->query("SELECT COUNT(*) as total FROM books");
    $totalBooks = 0;
    if ($totalResult) {
        $total = $totalResult->fetch_assoc();
        $totalBooks = (int) $total['total'];
    }

    respond(true, 'Categories fetched successfully', [
        'categories' => $categories,
        'category_list' => $categoryList,
        'total_books' => $totalBooks
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("Get Categories Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>