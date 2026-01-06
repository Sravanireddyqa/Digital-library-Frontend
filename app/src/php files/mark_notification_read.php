<?php
/**
 * Mark Notification as Read
 * ==========================
 * Mark one or all notifications as read
 */

require_once 'db.php';
setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(false, 'POST method required');
}

$input = getInput();

if (empty($input['user_id'])) {
    respond(false, 'Missing required field: user_id');
}

$userId = intval($input['user_id']);
$conn = getConnection();

try {
    if (isset($input['mark_all']) && $input['mark_all'] === true) {
        // Mark all notifications as read for this user
        $stmt = $conn->prepare("UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE");
        $stmt->bind_param("i", $userId);

        if ($stmt->execute()) {
            $affectedRows = $stmt->affected_rows;
            respond(true, "Marked $affectedRows notifications as read");
        } else {
            respond(false, 'Failed to mark notifications as read');
        }
    } else if (isset($input['notification_id'])) {
        // Mark specific notification as read
        $notificationId = intval($input['notification_id']);

        $stmt = $conn->prepare("UPDATE notifications SET is_read = TRUE WHERE id = ? AND user_id = ?");
        $stmt->bind_param("ii", $notificationId, $userId);

        if ($stmt->execute()) {
            if ($stmt->affected_rows > 0) {
                respond(true, 'Notification marked as read');
            } else {
                respond(false, 'Notification not found or already read');
            }
        } else {
            respond(false, 'Failed to mark notification as read');
        }
    } else {
        respond(false, 'Missing notification_id or mark_all parameter');
    }
} catch (Exception $e) {
    respond(false, 'Error: ' . $e->getMessage());
} finally {
    $conn->close();
}
?>