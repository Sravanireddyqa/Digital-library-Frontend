<?php
/**
 * Get Reader Stats API
 * Returns stats for a specific user
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

    // Get books read count (returned reservations)
    $booksReadQuery = "SELECT COUNT(*) as books_read FROM reservations 
                       WHERE user_id = ? AND LOWER(status) = 'returned'";
    $stmt = $conn->prepare($booksReadQuery);
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $booksReadResult = $stmt->get_result()->fetch_assoc();
    $booksRead = $booksReadResult['books_read'] ?? 0;
    $stmt->close();

    // Get active reservations count (pending or approved)
    $activeQuery = "SELECT COUNT(*) as active FROM reservations 
                    WHERE user_id = ? AND LOWER(status) IN ('pending', 'approved')";
    $stmt = $conn->prepare($activeQuery);
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $activeResult = $stmt->get_result()->fetch_assoc();
    $activeReservations = $activeResult['active'] ?? 0;
    $stmt->close();

    // Get wishlist count (only count items where book still exists)
    $wishlistCount = 0;
    $wishlistCheck = $conn->query("SHOW TABLES LIKE 'wishlist'");
    if ($wishlistCheck->num_rows > 0) {
        $wishlistQuery = "SELECT COUNT(*) as wishlist FROM wishlist w 
                          INNER JOIN books b ON w.book_id = b.id 
                          WHERE w.user_id = ?";
        $stmt = $conn->prepare($wishlistQuery);
        $stmt->bind_param("i", $userId);
        $stmt->execute();
        $wishlistResult = $stmt->get_result()->fetch_assoc();
        $wishlistCount = $wishlistResult['wishlist'] ?? 0;
        $stmt->close();
    }

    respond(true, 'Stats fetched successfully', [
        'stats' => [
            'books_read' => (int) $booksRead,
            'active_reservations' => (int) $activeReservations,
            'wishlist_items' => (int) $wishlistCount
        ]
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("Get Reader Stats Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>