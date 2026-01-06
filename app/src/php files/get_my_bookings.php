<?php
/**
 * Get My Bookings API
 * Returns reservations for a specific user
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

    // Build base URL for covers
    $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? 'https' : 'http';
    $host = $_SERVER['HTTP_HOST'];
    $basePath = dirname($_SERVER['SCRIPT_NAME']);
    $baseUrl = $protocol . '://' . $host . $basePath . '/';

    // Get mode parameter for filtering
    $mode = isset($_GET['mode']) ? strtolower($_GET['mode']) : 'all';

    // Build status filter based on mode
    $statusFilter = "";
    if ($mode === 'read') {
        $statusFilter = "AND LOWER(r.status) = 'returned'";
    } elseif ($mode === 'active') {
        $statusFilter = "AND LOWER(r.status) IN ('pending', 'approved')";
    }
    // All mode shows everything including cancelled

    $sql = "SELECT 
                r.id,
                r.book_id,
                COALESCE(b.title, 'Unknown Book') as book_title,
                COALESCE(b.author, 'Unknown Author') as author,
                COALESCE(b.cover_url, '') as cover_url,
                r.status,
                DATE_FORMAT(r.pickup_date, '%b %d, %Y') as pickup_date,
                DATE_FORMAT(r.created_at, '%b %d, %Y') as created_at
            FROM reservations r
            LEFT JOIN books b ON r.book_id = b.id
            WHERE r.user_id = ? $statusFilter
            ORDER BY r.created_at DESC";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $result = $stmt->get_result();

    $bookings = [];
    while ($row = $result->fetch_assoc()) {
        // Build full cover URL
        $coverUrl = $row['cover_url'] ?? '';
        if (!empty($coverUrl) && !preg_match('/^https?:\/\//', $coverUrl)) {
            $coverUrl = $baseUrl . $coverUrl;
        }

        $bookings[] = [
            'id' => (int) $row['id'],
            'book_id' => (int) $row['book_id'],
            'book_title' => $row['book_title'],
            'author' => $row['author'],
            'cover_url' => $coverUrl,
            'status' => $row['status'],
            'pickup_date' => $row['pickup_date'],
            'created_at' => $row['created_at']
        ];
    }

    respond(true, 'Bookings fetched successfully', [
        'bookings' => $bookings,
        'count' => count($bookings)
    ]);

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    error_log("Get My Bookings Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>