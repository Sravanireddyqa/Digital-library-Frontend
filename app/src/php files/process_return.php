<?php
/**
 * Process Return API
 * Handles book return with different conditions and creates transactions
 */

require_once 'db.php';
require_once 'send_notification.php';

// Set headers
setHeaders();

// Only allow POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $conn = getConnection();

    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);

    $reservationId = isset($input['reservation_id']) ? (int) $input['reservation_id'] : 0;
    $returnCondition = isset($input['condition']) ? strtolower($input['condition']) : 'safe';
    $daysLate = isset($input['days_late']) ? (int) $input['days_late'] : 0;
    $damageLevel = isset($input['damage_level']) ? strtolower($input['damage_level']) : 'minor';
    $notes = isset($input['notes']) ? trim($input['notes']) : '';

    // Fine rate per day for late returns (configurable)
    $finePerDay = 10.00;

    // Damage percentages
    $damagePercentages = [
        'minor' => 0.10,    // 10%
        'moderate' => 0.30, // 30%
        'severe' => 0.50    // 50%
    ];

    if ($reservationId <= 0) {
        respond(false, 'Invalid reservation ID');
    }

    if (!in_array($returnCondition, ['safe', 'late', 'damaged', 'lost'])) {
        respond(false, 'Invalid return condition');
    }

    // Get reservation details
    $reservationQuery = $conn->prepare("SELECT r.*, b.title as book_title, b.price as book_price, u.name as user_name
                                        FROM reservations r
                                        JOIN books b ON r.book_id = b.id
                                        JOIN users u ON r.user_id = u.id
                                        WHERE r.id = ?");
    $reservationQuery->bind_param("i", $reservationId);
    $reservationQuery->execute();
    $reservation = $reservationQuery->get_result()->fetch_assoc();

    if (!$reservation) {
        respond(false, 'Reservation not found');
    }

    $userId = $reservation['user_id'];
    $bookId = $reservation['book_id'];
    $bookTitle = $reservation['book_title'];
    $bookPrice = (float) $reservation['book_price'];

    // Get the deposit amount for this reservation
    $depositQuery = $conn->prepare("SELECT amount FROM transactions WHERE reservation_id = ? AND type = 'deposit' ORDER BY id DESC LIMIT 1");
    $depositQuery->bind_param("i", $reservationId);
    $depositQuery->execute();
    $depositResult = $depositQuery->get_result()->fetch_assoc();
    $depositAmount = $depositResult ? (float) $depositResult['amount'] : $bookPrice;

    // Calculate fine and refund based on condition
    $fineAmount = 0;
    $refundAmount = $depositAmount;
    $reason = $returnCondition;
    $notificationMessage = "";

    switch ($returnCondition) {
        case 'safe':
            // Full refund
            $refundAmount = $depositAmount;
            $fineAmount = 0;
            $notificationMessage = "Your book '$bookTitle' has been returned safely. Full deposit of ₹$depositAmount will be refunded.";
            break;

        case 'late':
            // Calculate late fine
            $fineAmount = $daysLate * $finePerDay;
            $refundAmount = max(0, $depositAmount - $fineAmount);
            $notificationMessage = "Your book '$bookTitle' was returned $daysLate days late. Fine: ₹$fineAmount. Refund: ₹$refundAmount.";
            break;

        case 'damaged':
            // Calculate damage fine based on level
            $damagePercent = isset($damagePercentages[$damageLevel]) ? $damagePercentages[$damageLevel] : 0.10;
            $fineAmount = $depositAmount * $damagePercent;
            $refundAmount = $depositAmount - $fineAmount;
            $notificationMessage = "Your book '$bookTitle' was returned with $damageLevel damage. Fine: ₹$fineAmount. Refund: ₹$refundAmount.";
            break;

        case 'lost':
            // Full book price as fine
            $fineAmount = $bookPrice;
            if ($bookPrice <= $depositAmount) {
                $refundAmount = $depositAmount - $bookPrice;
            } else {
                $refundAmount = 0;
                // Extra fine owed
                $extraFine = $bookPrice - $depositAmount;
            }
            $notificationMessage = "Your book '$bookTitle' was reported as lost. Full book price (₹$bookPrice) deducted. Refund: ₹$refundAmount.";
            break;
    }

    // Start transaction
    $conn->begin_transaction();

    try {
        // Create fine transaction if applicable
        if ($fineAmount > 0) {
            $fineStmt = $conn->prepare("INSERT INTO transactions (user_id, reservation_id, book_id, type, amount, status, reason, days_late, fine_rate, notes) VALUES (?, ?, ?, 'fine', ?, 'completed', ?, ?, ?, ?)");
            $fineStmt->bind_param("iiidsids", $userId, $reservationId, $bookId, $fineAmount, $reason, $daysLate, $finePerDay, $notes);
            $fineStmt->execute();
        }

        // Create refund transaction
        if ($refundAmount > 0) {
            $refundStmt = $conn->prepare("INSERT INTO transactions (user_id, reservation_id, book_id, type, amount, status, reason, notes) VALUES (?, ?, ?, 'refund', ?, 'completed', ?, ?)");
            $refundStmt->bind_param("iiidss", $userId, $reservationId, $bookId, $refundAmount, $reason, $notes);
            $refundStmt->execute();
        }

        // Create pending fine if book lost and price > deposit
        if ($returnCondition === 'lost' && isset($extraFine) && $extraFine > 0) {
            $pendingStmt = $conn->prepare("INSERT INTO transactions (user_id, reservation_id, book_id, type, amount, status, reason, notes) VALUES (?, ?, ?, 'pending_fine', ?, 'pending', 'lost', 'Extra amount owed for lost book')");
            $pendingStmt->bind_param("iiid", $userId, $reservationId, $bookId, $extraFine);
            $pendingStmt->execute();
        }

        // Update reservation status to returned
        $updateStmt = $conn->prepare("UPDATE reservations SET status = 'returned' WHERE id = ?");
        $updateStmt->bind_param("i", $reservationId);
        $updateStmt->execute();

        // Restore book stock
        $stockStmt = $conn->prepare("UPDATE books SET stock = stock + 1 WHERE id = ?");
        $stockStmt->bind_param("i", $bookId);
        $stockStmt->execute();

        $conn->commit();

        // Send notification to user
        $notificationType = 'book_returned';
        $notificationTitle = '📚 Book Returned';
        sendNotification($userId, $notificationType, $notificationTitle, $notificationMessage, [
            'type' => 'book_returned',
            'reservation_id' => strval($reservationId),
            'refund_amount' => strval($refundAmount),
            'fine_amount' => strval($fineAmount)
        ]);

        respond(true, 'Return processed successfully', [
            'reservation_id' => $reservationId,
            'condition' => $returnCondition,
            'deposit' => $depositAmount,
            'fine' => $fineAmount,
            'refund' => $refundAmount,
            'extra_fine' => isset($extraFine) ? $extraFine : 0
        ]);

    } catch (Exception $e) {
        $conn->rollback();
        throw $e;
    }

    $conn->close();

} catch (Exception $e) {
    error_log("Process Return Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>