<?php
/**
 * Get Transactions API
 * Returns list of all transactions with filters and summary
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

    // Check if transactions table exists
    $tableCheck = $conn->query("SHOW TABLES LIKE 'transactions'");
    if ($tableCheck->num_rows == 0) {
        respond(true, 'No transactions found', [
            'transactions' => [],
            'summary' => [
                'total_deposits' => 0,
                'total_refunds' => 0,
                'total_fines' => 0,
                'pending_refunds' => 0
            ]
        ]);
    }

    // Optional filters
    $type = isset($_GET['type']) ? strtolower(trim($_GET['type'])) : null;
    $status = isset($_GET['status']) ? strtolower(trim($_GET['status'])) : null;
    $userId = isset($_GET['user_id']) ? (int) $_GET['user_id'] : null;

    // Get summary totals
    $summaryQuery = "SELECT 
        COALESCE(SUM(CASE WHEN type = 'deposit' AND status = 'completed' THEN amount ELSE 0 END), 0) as total_deposits,
        COALESCE(SUM(CASE WHEN type = 'refund' AND status = 'completed' THEN amount ELSE 0 END), 0) as total_refunds,
        COALESCE(SUM(CASE WHEN type IN ('fine', 'pending_fine') AND status = 'completed' THEN amount ELSE 0 END), 0) as total_fines,
        COALESCE(COUNT(CASE WHEN type = 'refund' AND status = 'pending' THEN 1 END), 0) as pending_refunds,
        COALESCE(SUM(CASE WHEN type = 'refund' AND status = 'pending' THEN amount ELSE 0 END), 0) as pending_refunds_amount
        FROM transactions";

    $summaryResult = $conn->query($summaryQuery);
    $summary = $summaryResult->fetch_assoc();

    // Build where clause
    $whereClauses = [];
    $params = [];
    $paramTypes = "";

    if ($type && in_array($type, ['deposit', 'refund', 'fine', 'pending_fine'])) {
        $whereClauses[] = "t.type = ?";
        $params[] = $type;
        $paramTypes .= "s";
    }

    if ($status && in_array($status, ['completed', 'pending', 'cancelled'])) {
        $whereClauses[] = "t.status = ?";
        $params[] = $status;
        $paramTypes .= "s";
    }

    if ($userId) {
        $whereClauses[] = "t.user_id = ?";
        $params[] = $userId;
        $paramTypes .= "i";
    }

    $whereClause = count($whereClauses) > 0 ? "WHERE " . implode(" AND ", $whereClauses) : "";

    // List query
    $listQuery = "SELECT 
        t.id,
        t.user_id,
        t.reservation_id,
        t.book_id,
        t.type,
        t.amount,
        t.status,
        t.reason,
        t.days_late,
        t.fine_rate,
        t.notes,
        t.created_at,
        t.processed_at,
        COALESCE(u.name, 'Unknown User') as user_name,
        COALESCE(u.email, '') as user_email,
        COALESCE(b.title, 'Unknown Book') as book_title
    FROM transactions t
    LEFT JOIN users u ON t.user_id = u.id
    LEFT JOIN books b ON t.book_id = b.id
    $whereClause
    ORDER BY t.created_at DESC";

    if (count($params) > 0) {
        $stmt = $conn->prepare($listQuery);
        $stmt->bind_param($paramTypes, ...$params);
        $stmt->execute();
        $result = $stmt->get_result();
    } else {
        $result = $conn->query($listQuery);
    }

    if (!$result) {
        respond(false, 'Query failed: ' . $conn->error);
    }

    $transactions = [];
    while ($row = $result->fetch_assoc()) {
        $transactions[] = [
            'id' => (int) $row['id'],
            'transaction_id' => 'TXN-' . str_pad($row['id'], 5, '0', STR_PAD_LEFT),
            'user_id' => (int) $row['user_id'],
            'user_name' => $row['user_name'],
            'user_email' => $row['user_email'],
            'reservation_id' => $row['reservation_id'] ? (int) $row['reservation_id'] : null,
            'book_id' => $row['book_id'] ? (int) $row['book_id'] : null,
            'book_title' => $row['book_title'],
            'type' => $row['type'],
            'amount' => (float) $row['amount'],
            'status' => $row['status'],
            'reason' => $row['reason'],
            'days_late' => (int) $row['days_late'],
            'fine_rate' => (float) $row['fine_rate'],
            'notes' => $row['notes'],
            'created_at' => $row['created_at'],
            'processed_at' => $row['processed_at']
        ];
    }

    respond(true, 'Transactions fetched successfully', [
        'transactions' => $transactions,
        'count' => count($transactions),
        'summary' => [
            'total_deposits' => (float) $summary['total_deposits'],
            'total_refunds' => (float) $summary['total_refunds'],
            'total_fines' => (float) $summary['total_fines'],
            'pending_refunds' => (int) $summary['pending_refunds'],
            'pending_refunds_amount' => (float) $summary['pending_refunds_amount']
        ]
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("Get Transactions Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>