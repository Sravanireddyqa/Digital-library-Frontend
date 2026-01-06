<?php
/**
 * Check Due Dates - Automatic Reminder Script
 * Run this daily via cron job or Windows Task Scheduler
 * 
 * Windows Task: schtasks /create /tn "LibraryReminders" /tr "php C:\xampp\htdocs\digitallibrary_API\check_due_dates.php" /sc daily /st 09:00
 * Linux Cron: 0 9 * * * php /var/www/html/digitallibrary_API/check_due_dates.php
 */

require_once 'db.php';
require_once 'send_notification.php';

// For CLI execution
if (php_sapi_name() !== 'cli') {
    // Also allow web access for testing
    header('Content-Type: application/json');
}

try {
    $conn = getConnection();
    $today = date('Y-m-d');
    $tomorrow = date('Y-m-d', strtotime('+1 day'));
    $yesterday = date('Y-m-d', strtotime('-1 day'));

    $remindersCount = [
        'due_tomorrow' => 0,
        'due_today' => 0,
        'overdue' => 0
    ];

    // Get all approved reservations that need reminders
    // Use due_date (user's selected return date) for reminder calculation
    $query = $conn->query("SELECT r.*, r.due_date as return_date, b.title as book_title, u.id as user_id, u.name as user_name
                           FROM reservations r
                           JOIN books b ON r.book_id = b.id
                           JOIN users u ON r.user_id = u.id
                           WHERE r.status = 'approved'
                           AND r.due_date IS NOT NULL
                           AND r.due_date <= DATE_ADD(CURDATE(), INTERVAL 1 DAY)");

    while ($reservation = $query->fetch_assoc()) {
        $returnDate = $reservation['return_date']; // This is due_date from query
        $userId = $reservation['user_id'];
        $bookTitle = $reservation['book_title'];
        $reservationId = $reservation['id'];

        // Check if reminder already sent today
        $checkSent = $conn->prepare("SELECT id FROM notifications 
                                     WHERE user_id = ? 
                                     AND type LIKE '%reminder%'
                                     AND JSON_EXTRACT(data, '$.reservation_id') = ?
                                     AND DATE(created_at) = CURDATE()");
        $resIdStr = strval($reservationId);
        $checkSent->bind_param("is", $userId, $resIdStr);
        $checkSent->execute();
        if ($checkSent->get_result()->num_rows > 0) {
            continue; // Already sent today
        }

        // Determine reminder type based on date
        if ($returnDate == $tomorrow) {
            // Due tomorrow
            $title = "📚 Book Due Tomorrow";
            $message = "Your book '$bookTitle' is due tomorrow. Don't forget to return it!";
            $type = "due_tomorrow_reminder";
            $remindersCount['due_tomorrow']++;
        } else if ($returnDate == $today) {
            // Due today
            $title = "📅 Book Due Today!";
            $message = "Your book '$bookTitle' is due TODAY! Please return it to avoid late fines.";
            $type = "due_today_reminder";
            $remindersCount['due_today']++;
        } else if ($returnDate < $today) {
            // Overdue - calculate days
            $daysOverdue = floor((strtotime($today) - strtotime($returnDate)) / 86400);
            $title = "⚠️ Book Overdue!";
            $message = "Your book '$bookTitle' is $daysOverdue day(s) overdue! Return immediately to avoid additional fines.";
            $type = "overdue_reminder";
            $remindersCount['overdue']++;
        } else {
            continue; // Not due yet
        }

        // Send notification
        $data = [
            'type' => $type,
            'reservation_id' => strval($reservationId),
            'book_title' => $bookTitle,
            'return_date' => $returnDate
        ];

        sendNotification($userId, $type, $title, $message, $data);

        echo "Reminder sent to user $userId for book '$bookTitle'\n";
    }

    $totalSent = array_sum($remindersCount);

    $result = [
        'success' => true,
        'message' => "Reminder check completed",
        'date' => $today,
        'reminders_sent' => $remindersCount,
        'total' => $totalSent
    ];

    if (php_sapi_name() !== 'cli') {
        echo json_encode($result, JSON_PRETTY_PRINT);
    } else {
        echo "\n=== Due Date Reminder Check ===\n";
        echo "Date: $today\n";
        echo "Due Tomorrow: " . $remindersCount['due_tomorrow'] . "\n";
        echo "Due Today: " . $remindersCount['due_today'] . "\n";
        echo "Overdue: " . $remindersCount['overdue'] . "\n";
        echo "Total Sent: $totalSent\n";
    }

    $conn->close();

} catch (Exception $e) {
    error_log("Check Due Dates Error: " . $e->getMessage());
    $error = ['success' => false, 'message' => $e->getMessage()];

    if (php_sapi_name() !== 'cli') {
        echo json_encode($error);
    } else {
        echo "Error: " . $e->getMessage() . "\n";
    }
}
?>