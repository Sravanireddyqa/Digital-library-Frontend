<?php
/**
 * Send Reminder API
 * Admin sends manual reminder to user about their reservation
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
    $reminderType = isset($input['type']) ? $input['type'] : 'general'; // general, due_soon, overdue

    if ($reservationId <= 0) {
        respond(false, 'Invalid reservation ID');
    }

    // Get reservation details
    $query = $conn->prepare("SELECT r.*, b.title as book_title, u.name as user_name, u.id as user_id
                             FROM reservations r
                             JOIN books b ON r.book_id = b.id
                             JOIN users u ON r.user_id = u.id
                             WHERE r.id = ?");
    $query->bind_param("i", $reservationId);
    $query->execute();
    $reservation = $query->get_result()->fetch_assoc();

    if (!$reservation) {
        respond(false, 'Reservation not found');
    }

    $userId = $reservation['user_id'];
    $bookTitle = $reservation['book_title'];
    $dueDate = $reservation['due_date']; // Use due_date instead of return_date

    // If due_date is not set, use pickup_date + 7 days as default
    if (empty($dueDate)) {
        $pickupDate = $reservation['pickup_date'] ?? $reservation['date'];
        if ($pickupDate) {
            $due = new DateTime($pickupDate);
            $due->add(new DateInterval('P7D')); // Add 7 days
            $dueDate = $due->format('Y-m-d');
        } else {
            // No date available, send general reminder
            $title = "📖 Book Reminder";
            $message = "Reminder: You have '$bookTitle' checked out. Please return it soon!";
            $type = "general_reminder";

            $data = [
                'type' => $type,
                'reservation_id' => strval($reservationId),
                'book_title' => $bookTitle
            ];

            sendNotification($userId, $type, $title, $message, $data);
            respond(true, 'Reminder sent successfully', [
                'user_id' => $userId,
                'book_title' => $bookTitle,
                'message' => $message
            ]);
            exit;
        }
    }

    // Calculate days until/after due date
    $today = new DateTime();
    $dueDateObj = new DateTime($dueDate);
    $diff = $today->diff($dueDateObj);
    $daysLeft = $diff->invert ? -$diff->days : $diff->days;

    // Prepare notification based on days left
    if ($daysLeft < 0) {
        // Overdue
        $title = "⚠️ Overdue Book Reminder";
        $message = "Your book '$bookTitle' is " . abs($daysLeft) . " days overdue! Please return it immediately to avoid additional fines.";
        $type = "overdue_reminder";
    } else if ($daysLeft == 0) {
        // Due today
        $title = "📅 Book Due Today";
        $message = "Your book '$bookTitle' is due today! Please return it to avoid late fines.";
        $type = "due_today_reminder";
    } else if ($daysLeft == 1) {
        // Due tomorrow
        $title = "📚 Book Due Tomorrow";
        $message = "Your book '$bookTitle' is due tomorrow. Don't forget to return it!";
        $type = "due_tomorrow_reminder";
    } else if ($daysLeft <= 3) {
        // Due soon
        $title = "📚 Book Due Soon";
        $message = "Your book '$bookTitle' is due in $daysLeft days. Don't forget to return it!";
        $type = "due_soon_reminder";
    } else {
        // General reminder
        $title = "📖 Book Reminder";
        $message = "Reminder: You have '$bookTitle' checked out. Due date: $dueDate.";
        $type = "general_reminder";
    }

    // Send notification
    $data = [
        'type' => $type,
        'reservation_id' => strval($reservationId),
        'book_title' => $bookTitle,
        'due_date' => $dueDate,
        'days_left' => strval($daysLeft)
    ];

    sendNotification($userId, $type, $title, $message, $data);

    respond(true, 'Reminder sent successfully', [
        'user_id' => $userId,
        'book_title' => $bookTitle,
        'days_left' => $daysLeft,
        'message' => $message
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("Send Reminder Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>