<?php
/**
 * Update Reservation Status API
 * Approves or rejects a reservation
 */

require_once 'db.php';

// Set headers
setHeaders();

// Only allow POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

// Get JSON input
$input = getInput();

// Validate
if (empty($input['reservation_id'])) {
    respond(false, 'Reservation ID is required');
}
if (empty($input['status'])) {
    respond(false, 'Status is required');
}

$reservationId = intval($input['reservation_id']);
$status = strtolower(trim($input['status']));

// Validate status
if (!in_array($status, ['pending', 'approved', 'rejected', 'returned'])) {
    respond(false, 'Invalid status. Must be pending, approved, rejected, or returned');
}

try {
    $conn = getConnection();

    // Get reservation details with book and user info
    $checkStmt = $conn->prepare("SELECT r.id, r.user_id, r.book_id, r.status as current_status, b.title as book_title, u.name as user_name 
                                  FROM reservations r 
                                  LEFT JOIN books b ON r.book_id = b.id 
                                  LEFT JOIN users u ON r.user_id = u.id 
                                  WHERE r.id = ?");
    $checkStmt->bind_param("i", $reservationId);
    $checkStmt->execute();
    $result = $checkStmt->get_result();

    if ($result->num_rows === 0) {
        respond(false, 'Reservation not found');
    }

    $reservation = $result->fetch_assoc();
    $checkStmt->close();

    $userId = $reservation['user_id'];
    $bookTitle = $reservation['book_title'];
    $userName = $reservation['user_name'];
    $currentStatus = $reservation['current_status'];

    // Check if updated_at column exists
    $columnsResult = $conn->query("SHOW COLUMNS FROM reservations LIKE 'updated_at'");
    $hasUpdatedAt = $columnsResult->num_rows > 0;

    // Update status
    if ($hasUpdatedAt) {
        $stmt = $conn->prepare("UPDATE reservations SET status = ?, updated_at = NOW() WHERE id = ?");
    } else {
        $stmt = $conn->prepare("UPDATE reservations SET status = ? WHERE id = ?");
    }
    $stmt->bind_param("si", $status, $reservationId);

    if ($stmt->execute()) {
        // Include notification helper
        require_once 'send_notification.php';

        // ============================================
        // SET UP NOTIFICATIONS BASED ON STATUS
        // ============================================

        if ($status === 'approved') {
            // USER Notification - Reservation Approved
            $userNotification = [
                'title' => "✅ Reservation Approved",
                'body' => "Your reservation for '$bookTitle' has been approved! Please collect on time."
            ];
            $userData = [
                'type' => 'reservation_approved',
                'reservation_id' => strval($reservationId),
                'book_title' => $bookTitle
            ];

        } elseif ($status === 'rejected') {
            // USER Notification - Reservation Rejected
            $userNotification = [
                'title' => "❌ Reservation Rejected",
                'body' => "Your reservation for '$bookTitle' was rejected. Please contact library for details."
            ];
            $userData = [
                'type' => 'reservation_rejected',
                'reservation_id' => strval($reservationId),
                'book_title' => $bookTitle
            ];

            // Restore book stock on rejection (only if was approved or pending)
            if (in_array($currentStatus, ['pending', 'approved'])) {
                $conn->query("UPDATE books SET stock = stock + 1 WHERE id = " . $reservation['book_id']);
            }

        } elseif ($status === 'returned') {
            // USER Notification - Book Returned
            $userNotification = [
                'title' => "📚 Book Returned",
                'body' => "Thank you for returning '$bookTitle'. Your deposit will be refunded."
            ];
            $userData = [
                'type' => 'book_returned',
                'reservation_id' => strval($reservationId),
                'book_title' => $bookTitle
            ];

            // Restore book stock on return (only if was approved)
            if ($currentStatus === 'approved') {
                $conn->query("UPDATE books SET stock = stock + 1 WHERE id = " . $reservation['book_id']);
            }
        }

        // Send notification to USER if status is approved, rejected, or returned
        if (in_array($status, ['approved', 'rejected', 'returned'])) {
            // Get user's FCM token and send push notification
            $tokenResult = $conn->query("SELECT token FROM fcm_tokens WHERE user_id = $userId");
            if ($tokenRow = $tokenResult->fetch_assoc()) {
                sendFCMMessage($tokenRow['token'], $userNotification, $userData);
            }

            // Save in-app notification for user
            $notifTitle = $userNotification['title'];
            $notifMessage = $userNotification['body'];
            $notifData = json_encode($userData);
            $notifType = $userData['type'];
            $stmt2 = $conn->prepare("INSERT INTO notifications (user_id, type, title, message, data, created_at) VALUES (?, ?, ?, ?, ?, NOW())");
            $stmt2->bind_param("issss", $userId, $notifType, $notifTitle, $notifMessage, $notifData);
            $stmt2->execute();
            $stmt2->close();
        }

        respond(true, 'Reservation ' . $status . ' successfully');
    } else {
        respond(false, 'Failed to update reservation');
    }

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    error_log("Update Reservation Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>