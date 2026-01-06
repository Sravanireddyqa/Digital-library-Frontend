<?php
/**
 * Get Reservations API
 * Returns list of all reservations with book and user details
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
    $conn = getConnection();

    // Check if reservations table exists
    $tableCheck = $conn->query("SHOW TABLES LIKE 'reservations'");
    if ($tableCheck->num_rows == 0) {
        respond(true, 'No reservations found', [
            'reservations' => [],
            'count' => 0,
            'pending_count' => 0,
            'approved_count' => 0,
            'rejected_count' => 0
        ]);
    }

    // Optional status filter
    $status = isset($_GET['status']) ? strtolower(trim($_GET['status'])) : null;

    // Count query
    $countSql = "SELECT 
        COUNT(*) as total,
        SUM(CASE WHEN LOWER(status) = 'pending' THEN 1 ELSE 0 END) as pending,
        SUM(CASE WHEN LOWER(status) = 'approved' THEN 1 ELSE 0 END) as approved,
        SUM(CASE WHEN LOWER(status) = 'rejected' THEN 1 ELSE 0 END) as rejected,
        SUM(CASE WHEN LOWER(status) = 'returned' THEN 1 ELSE 0 END) as returned,
        SUM(CASE WHEN LOWER(status) = 'cancelled' THEN 1 ELSE 0 END) as cancelled
        FROM reservations";

    $countResult = $conn->query($countSql);
    $pendingCount = 0;
    $approvedCount = 0;
    $rejectedCount = 0;
    $cancelledCount = 0;

    if ($countResult && $countRow = $countResult->fetch_assoc()) {
        $pendingCount = (int) $countRow['pending'];
        $approvedCount = (int) $countRow['approved'];
        $rejectedCount = (int) $countRow['rejected'];
        $cancelledCount = (int) $countRow['cancelled'];
    }

    // Check available columns
    $columnsResult = $conn->query("SHOW COLUMNS FROM reservations");
    $columns = [];
    while ($col = $columnsResult->fetch_assoc()) {
        $columns[] = $col['Field'];
    }

    // Build dynamic query based on available columns
    $hasLibraryId = in_array('library_id', $columns);
    $hasLibrary = in_array('library', $columns);
    $hasPickupDate = in_array('pickup_date', $columns);
    $hasReservationDate = in_array('reservation_date', $columns);
    $hasTimeSlot = in_array('time_slot', $columns);
    $hasPickupTime = in_array('pickup_time', $columns);
    $hasCreatedAt = in_array('created_at', $columns);
    $hasRequestedAt = in_array('requested_at', $columns);
    $hasCancelReason = in_array('cancel_reason', $columns);

    // Where clause - now includes cancelled
    $whereClause = "";
    if ($status && in_array($status, ['pending', 'approved', 'rejected', 'returned', 'cancelled'])) {
        $whereClause = " WHERE LOWER(r.status) = '" . $conn->real_escape_string($status) . "'";
    }

    // List query
    $listSql = "SELECT 
                r.id, 
                r.book_id,
                r.user_id,
                COALESCE(b.title, 'Unknown Book') AS book_title,
                COALESCE(b.cover_url, '') AS book_cover,
                COALESCE(u.name, 'Unknown User') AS user_name,
                COALESCE(u.email, '') AS user_email,
                " . ($hasLibraryId ? "COALESCE(l.name, 'Central Library')" : ($hasLibrary ? "COALESCE(r.library, 'Central Library')" : "'Central Library'")) . " AS library,
                " . ($hasPickupDate ? "r.pickup_date" : ($hasReservationDate ? "r.reservation_date" : "DATE(NOW())")) . " AS pickup_date,
                " . ($hasTimeSlot ? "r.time_slot" : ($hasPickupTime ? "r.pickup_time" : "'10:00 AM - 12:00 PM'")) . " AS pickup_time,
                LOWER(COALESCE(r.status, 'pending')) AS status,
                " . ($hasCreatedAt ? "r.created_at" : ($hasRequestedAt ? "r.requested_at" : "NOW()")) . " AS requested_at,
                " . ($hasCancelReason ? "COALESCE(r.cancel_reason, '')" : "''") . " AS cancel_reason
            FROM reservations r
            LEFT JOIN books b ON r.book_id = b.id
            LEFT JOIN users u ON r.user_id = u.id
            " . ($hasLibraryId ? "LEFT JOIN libraries l ON r.library_id = l.id" : "") . "
            $whereClause
            ORDER BY r.id DESC";

    $result = $conn->query($listSql);

    if (!$result) {
        respond(false, 'Query failed: ' . $conn->error);
    }

    $reservations = [];

    while ($row = $result->fetch_assoc()) {
        $reservations[] = [
            'id' => (int) $row['id'],
            'reservation_id' => 'RES-' . str_pad($row['id'], 3, '0', STR_PAD_LEFT),
            'book_id' => (int) $row['book_id'],
            'book_title' => $row['book_title'],
            'book_cover' => $row['book_cover'],
            'user_id' => (int) $row['user_id'],
            'user_name' => $row['user_name'],
            'user_email' => $row['user_email'],
            'library' => $row['library'],
            'pickup_date' => $row['pickup_date'],
            'pickup_time' => $row['pickup_time'],
            'status' => $row['status'],
            'requested_at' => $row['requested_at'],
            'cancel_reason' => $row['cancel_reason']
        ];
    }

    respond(true, 'Reservations fetched successfully', [
        'reservations' => $reservations,
        'count' => count($reservations),
        'pending_count' => $pendingCount,
        'approved_count' => $approvedCount,
        'rejected_count' => $rejectedCount,
        'cancelled_count' => $cancelledCount
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("Get Reservations Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>