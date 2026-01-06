<?php
/**
 * Get Notifications
 * =================
 * Fetch all notifications for a user
 */

require_once 'db.php';
setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    respond(false, 'GET method required');
}

// Get user_id from query parameter
$userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

if ($userId <= 0) {
    respond(false, 'Invalid user_id');
}

$conn = getConnection();

try {
    // Optional filters
    $unreadOnly = isset($_GET['unread_only']) && $_GET['unread_only'] === 'true';
    $type = isset($_GET['type']) ? $_GET['type'] : null;
    $limit = isset($_GET['limit']) ? intval($_GET['limit']) : 50;
    $offset = isset($_GET['offset']) ? intval($_GET['offset']) : 0;

    // Build query
    $query = "SELECT id, user_id, type, title, message, data, is_read, created_at 
              FROM notifications 
              WHERE user_id = ?";

    $params = [$userId];
    $types = "i";

    if ($unreadOnly) {
        $query .= " AND is_read = FALSE";
    }

    if ($type) {
        $query .= " AND type = ?";
        $params[] = $type;
        $types .= "s";
    }

    $query .= " ORDER BY created_at DESC LIMIT ? OFFSET ?";
    $params[] = $limit;
    $params[] = $offset;
    $types .= "ii";

    $stmt = $conn->prepare($query);
    $stmt->bind_param($types, ...$params);
    $stmt->execute();
    $result = $stmt->get_result();

    $notifications = [];
    while ($row = $result->fetch_assoc()) {
        $notifications[] = [
            'id' => $row['id'],
            'user_id' => intval($row['user_id']),
            'type' => $row['type'],
            'title' => $row['title'],
            'message' => $row['message'],
            'data' => $row['data'] ? json_decode($row['data'], true) : null,
            'is_read' => (bool) $row['is_read'],
            'created_at' => $row['created_at']
        ];
    }

    // Get unread count
    $stmt = $conn->prepare("SELECT COUNT(*) as count FROM notifications WHERE user_id = ? AND is_read = FALSE");
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $countResult = $stmt->get_result();
    $unreadCount = $countResult->fetch_assoc()['count'];

    respond(true, 'Notifications retrieved successfully', [
        'notifications' => $notifications,
        'unread_count' => intval($unreadCount),
        'total_count' => count($notifications)
    ]);

} catch (Exception $e) {
    respond(false, 'Error: ' . $e->getMessage());
} finally {
    $conn->close();
}
?>