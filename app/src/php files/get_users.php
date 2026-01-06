<?php
/**
 * Get Users API
 * Returns list of all users with stats
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $conn = getConnection();

    // Check if status column exists, add if not
    $colCheck = $conn->query("SHOW COLUMNS FROM users LIKE 'status'");
    if ($colCheck->num_rows == 0) {
        $conn->query("ALTER TABLE users ADD COLUMN status ENUM('active', 'blocked') DEFAULT 'active'");
    }

    // Check if type column exists, add if not
    $colCheck = $conn->query("SHOW COLUMNS FROM users LIKE 'type'");
    if ($colCheck->num_rows == 0) {
        $conn->query("ALTER TABLE users ADD COLUMN type ENUM('user', 'admin') DEFAULT 'user'");
    }

    // Check if joined_date column exists, add if not
    $colCheck = $conn->query("SHOW COLUMNS FROM users LIKE 'joined_date'");
    if ($colCheck->num_rows == 0) {
        $conn->query("ALTER TABLE users ADD COLUMN joined_date DATE DEFAULT CURRENT_DATE");
    }

    // Get all users
    $sql = "SELECT 
                u.id,
                u.name,
                u.email,
                COALESCE(u.type, 'user') as type,
                COALESCE(u.status, 'active') as status,
                COALESCE(u.joined_date, DATE(u.created_at)) as joined_date,
                (SELECT COUNT(*) FROM reservations r WHERE r.user_id = u.id) as reservations_count
            FROM users u
            ORDER BY u.id DESC";

    $result = $conn->query($sql);

    if (!$result) {
        respond(false, 'Query failed: ' . $conn->error);
    }

    $users = [];
    while ($row = $result->fetch_assoc()) {
        $users[] = [
            'id' => (int) $row['id'],
            'name' => $row['name'],
            'email' => $row['email'],
            'type' => $row['type'],
            'status' => $row['status'],
            'joined_date' => $row['joined_date'],
            'reservations_count' => (int) $row['reservations_count']
        ];
    }

    respond(true, 'Users fetched successfully', [
        'users' => $users,
        'count' => count($users)
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("Get Users Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>