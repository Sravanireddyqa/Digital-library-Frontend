<?php
/**
 * Notification Integration Examples
 * ==================================
 * This file shows how to integrate notifications into existing PHP files
 * Copy the relevant code snippets into your existing files
 */

require_once 'send_notification.php';

// =====================================================
// 1. NEW BOOK ADDED - Integrate into add_book.php
// =====================================================
/*
After successfully adding a book in add_book.php, add this code:

// Get the book details
$bookTitle = $input['title']; // or however you get the book title

// Send notification to all users
sendBroadcastNotification(
    'new_book',
    '📚 New Book Added',
    "Check out our latest addition: $bookTitle",
    ['book_id' => $bookId]
);
*/

// =====================================================
// 2. RESERVATION CONFIRMED - Integrate into create_reservation.php
// =====================================================
/*
After successfully creating a reservation in create_reservation.php:

// Get reservation details
$userId = $input['user_id'];
$bookTitle = $bookDetails['title']; // Get from your book lookup

// Send confirmation notification
sendNotification(
    $userId,
    'reservation_confirmed',
    '✅ Reservation Confirmed',
    "Your reservation for \"$bookTitle\" has been confirmed.",
    [
        'reservation_id' => $reservationId,
        'book_id' => $bookId
    ]
);
*/

// =====================================================
// 3. RESERVATION REJECTED - Integrate into update_reservation.php
// =====================================================
/*
When rejecting a reservation in update_reservation.php:

if ($newStatus === 'rejected') {
    $userId = $reservation['user_id'];
    $bookTitle = $reservation['book_title'];
    $reason = isset($input['reason']) ? $input['reason'] : 'Book currently unavailable';
    
    sendNotification(
        $userId,
        'reservation_rejected',
        '❌ Reservation Rejected',
        "Sorry! \"$bookTitle\" is $reason.",
        [
            'reservation_id' => $reservationId,
            'reason' => $reason
        ]
    );
}
*/

// =====================================================
// 4. BOOK READY FOR PICKUP - Integrate into update_reservation.php
// =====================================================
/*
When reservation status changes to 'ready':

if ($newStatus === 'ready') {
    $userId = $reservation['user_id'];
    $bookTitle = $reservation['book_title'];
    $pickupTime = '5 PM'; // Configure as needed
    
    sendNotification(
        $userId,
        'book_ready',
        '📦 Book Ready for Pickup',
        "Your book \"$bookTitle\" is ready for pickup. Collect before $pickupTime.",
        [
            'reservation_id' => $reservationId,
            'book_id' => $bookId,
            'pickup_deadline' => $pickupTime
        ]
    );
}
*/

// =====================================================
// 5. RETURN REMINDER - Create send_return_reminders.php (Cron Job)
// =====================================================
/*
Create a new file: send_return_reminders.php
Schedule this to run daily via cron

<?php
require_once 'db.php';
require_once 'send_notification.php';

$conn = getConnection();

// Get reservations due tomorrow
$tomorrow = date('Y-m-d', strtotime('+1 day'));
$stmt = $conn->prepare("
    SELECT r.id, r.user_id, b.title, r.due_date 
    FROM reservations r
    JOIN books b ON r.book_id = b.id
    WHERE DATE(r.due_date) = ? 
    AND r.status = 'issued'
");
$stmt->bind_param("s", $tomorrow);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    sendNotification(
        $row['user_id'],
        'return_reminder',
        '⏰ Return Reminder',
        "Please return \"" . $row['title'] . "\" by tomorrow.",
        [
            'reservation_id' => $row['id'],
            'due_date' => $row['due_date']
        ]
    );
}

$conn->close();
echo "Return reminders sent!\n";
?>
*/

// =====================================================
// 6. OVERDUE FINE ALERT - Create send_overdue_alerts.php (Cron Job)
// =====================================================
/*
Create a new file: send_overdue_alerts.php
Schedule this to run daily via cron

<?php
require_once 'db.php';
require_once 'send_notification.php';

$conn = getConnection();

// Get overdue reservations
$today = date('Y-m-d');
$stmt = $conn->prepare("
    SELECT r.id, r.user_id, b.title, r.due_date,
           DATEDIFF(NOW(), r.due_date) as days_overdue
    FROM reservations r
    JOIN books b ON r.book_id = b.id
    WHERE DATE(r.due_date) < ? 
    AND r.status = 'issued'
");
$stmt->bind_param("s", $today);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $daysOverdue = $row['days_overdue'];
    $finePerDay = 10; // ₹10 per day
    $totalFine = $daysOverdue * $finePerDay;
    
    sendNotification(
        $row['user_id'],
        'overdue_fine',
        '⚠️ Overdue Fine Alert',
        "Your book \"" . $row['title'] . "\" is overdue. Fine ₹$totalFine ($daysOverdue days × ₹$finePerDay/day).",
        [
            'reservation_id' => $row['id'],
            'days_overdue' => $daysOverdue,
            'fine_amount' => $totalFine
        ]
    );
}

$conn->close();
echo "Overdue alerts sent!\n";
?>
*/

// =====================================================
// 7. ACCOUNT BLOCKED - Create block_user.php (Admin Action)
// =====================================================
/*
Create a new file: block_user.php for admin to block users

<?php
require_once 'db.php';
require_once 'send_notification.php';
setHeaders();

$input = getInput();
$userId = $input['user_id'];
$reason = isset($input['reason']) ? $input['reason'] : 'Multiple late returns';
$duration = isset($input['duration']) ? $input['duration'] : 'temporarily';

$conn = getConnection();

// Update user status
$stmt = $conn->prepare("UPDATE users SET status = 'blocked' WHERE id = ?");
$stmt->bind_param("i", $userId);
$stmt->execute();

// Send notification
sendNotification(
    $userId,
    'account_blocked',
    '🚫 Account Alert',
    "Your account has been $duration blocked due to $reason.",
    [
        'reason' => $reason,
        'duration' => $duration
    ]
);

$conn->close();
respond(true, 'User blocked and notified');
?>
*/

// =====================================================
// 8. GENERAL ANNOUNCEMENT - Create send_broadcast.php (Admin Action)
// =====================================================
/*
Create a new file: send_broadcast.php for admin announcements

<?php
require_once 'send_notification.php';
setHeaders();

$input = getInput();
$title = isset($input['title']) ? $input['title'] : '📢 Announcement';
$message = $input['message'];

if (empty($message)) {
    respond(false, 'Message is required');
}

// Send to all users
$success = sendBroadcastNotification(
    'general_announcement',
    $title,
    $message
);

if ($success) {
    respond(true, 'Announcement sent to all users');
} else {
    respond(false, 'Failed to send announcement');
}
?>
*/

// =====================================================
// 9. PROFILE UPDATED - Integrate into update_profile.php and change_password.php
// =====================================================
/*
After successfully updating profile/password:

// In update_profile.php or change_password.php
$userId = $input['user_id'];

sendNotification(
    $userId,
    'profile_updated',
    '🔐 Profile Updated',
    'Your profile details were successfully updated.',
    [
        'update_type' => 'profile' // or 'password'
    ]
);
*/

// =====================================================
// HOW TO USE
// =====================================================
/*
1. Copy the SQL schema from notifications_schema.sql and run it in phpMyAdmin

2. Copy existing PHP files to your XAMPP htdocs folder:
   - register_fcm_token.php
   - get_notifications.php
   - mark_notification_read.php
   - delete_notification.php
   - send_notification.php

3. Update send_notification.php with your Firebase Server Key:
   - Go to Firebase Console → Project Settings → Cloud Messaging
   - Copy the "Server key"
   - Replace 'YOUR_FIREBASE_SERVER_KEY_HERE' in send_notification.php

4. Integrate notifications into existing files using the examples above

5. (Optional) Set up cron jobs for:
   - send_return_reminders.php (run daily)
   - send_overdue_alerts.php (run daily)

6. Test each notification type from the admin panel or postman
*/
?>