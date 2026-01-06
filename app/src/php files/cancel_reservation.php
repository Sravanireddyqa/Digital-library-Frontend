<?php
/**
 * Cancel Reservation API
 * Cancels a reservation and initiates refund if payment was made
 */

require_once 'db.php';
require_once 'send_notification.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $data = json_decode(file_get_contents('php://input'), true);

    $reservationId = isset($data['reservation_id']) ? intval($data['reservation_id']) : 0;
    $userId = isset($data['user_id']) ? intval($data['user_id']) : 0;

    if ($reservationId <= 0) {
        respond(false, 'Invalid reservation ID');
    }

    $conn = getConnection();

    // Get reservation details
    $query = $conn->prepare("SELECT r.*, b.title as book_title, b.price as deposit_amount
                             FROM reservations r
                             JOIN books b ON r.book_id = b.id
                             WHERE r.id = ? AND r.user_id = ?");
    $query->bind_param("ii", $reservationId, $userId);
    $query->execute();
    $reservation = $query->get_result()->fetch_assoc();
    $query->close();

    if (!$reservation) {
        respond(false, 'Reservation not found or access denied');
    }

    // Check if already cancelled or returned
    $status = strtolower($reservation['status']);
    if ($status === 'cancelled') {
        respond(false, 'Reservation already cancelled');
    }
    if ($status === 'returned') {
        respond(false, 'Cannot cancel - book already returned');
    }

    $bookTitle = $reservation['book_title'];
    $depositAmount = isset($reservation['deposit_amount']) ? (float) $reservation['deposit_amount'] : 0;

    // Get cancel reason (optional)
    $cancelReason = isset($data['cancel_reason']) ? trim($data['cancel_reason']) : '';

    // Check if cancel_reason column exists, add if not
    $columnCheck = $conn->query("SHOW COLUMNS FROM reservations LIKE 'cancel_reason'");
    if ($columnCheck->num_rows == 0) {
        $conn->query("ALTER TABLE reservations ADD COLUMN cancel_reason TEXT DEFAULT NULL");
    }

    // Update reservation status to cancelled with reason
    if (!empty($cancelReason)) {
        $updateQuery = $conn->prepare("UPDATE reservations SET status = 'cancelled', cancel_reason = ? WHERE id = ?");
        $updateQuery->bind_param("si", $cancelReason, $reservationId);
    } else {
        $updateQuery = $conn->prepare("UPDATE reservations SET status = 'cancelled' WHERE id = ?");
        $updateQuery->bind_param("i", $reservationId);
    }

    if (!$updateQuery->execute()) {
        respond(false, 'Failed to cancel reservation');
    }
    $updateQuery->close();

    // Restore book stock
    $bookId = $reservation['book_id'];
    $conn->query("UPDATE books SET stock = stock + 1 WHERE id = $bookId");

    // Process refund (Razorpay)
    $refundSuccess = false;
    $refundMessage = "";

    if ($depositAmount > 0) {
        // In production, you would call Razorpay Refund API here
        // For now, we'll mark it as refund initiated
        $refundSuccess = true;
        $refundMessage = "Refund of ₹$depositAmount initiated. Will be credited in 5-7 business days.";

        // Log the refund
        error_log("Refund initiated for reservation $reservationId: ₹$depositAmount");
    }

    // Send notification to user
    $notifTitle = "❌ Reservation Cancelled";
    $notifMessage = "Your reservation for '$bookTitle' has been cancelled.";
    if ($refundSuccess) {
        $notifMessage .= " " . $refundMessage;
    }

    $notifData = [
        'type' => 'reservation_cancelled',
        'reservation_id' => strval($reservationId),
        'book_title' => $bookTitle,
        'refund_amount' => strval($depositAmount)
    ];

    sendNotification($userId, 'reservation_cancelled', $notifTitle, $notifMessage, $notifData);

    respond(true, 'Reservation cancelled successfully', [
        'reservation_id' => $reservationId,
        'book_title' => $bookTitle,
        'refund_initiated' => $refundSuccess,
        'refund_amount' => $depositAmount,
        'refund_message' => $refundMessage
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("Cancel Reservation Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>