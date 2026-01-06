<?php
/**
 * Delete Notification
 * ===================
 * Delete a specific notification
 */

require_once 'db.php';
setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(false, 'POST method required');
}

$input = getInput();

if (empty($input['user_id']) || empty($input['notification_id'])) {
    respond(false, 'Missing required fields: user_id and notification_id');
}

$userId = intval($input['user_id']);
$notificationId = intval($input['notification_id']);

$conn = getConnection();

try {
    // Delete notification (verify it belongs to the user)
    $stmt = $conn->prepare("DELETE FROM notifications WHERE id = ? AND user_id = ?");
    $stmt->bind_param("ii", $notificationId, $userId);

    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            respond(true, 'Notification deleted successfully');
        } else {
            respond(false, 'Notification not found or does not belong to user');
        }
    } else {
        respond(false, 'Failed to delete notification');
    }
} catch (Exception $e) {
    respond(false, 'Error: ' . $e->getMessage());
} finally {
    $conn->close();
}
?>