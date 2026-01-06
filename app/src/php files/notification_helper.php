<?php
/**
 * Smart Digital Library Notification System
 * Two-way notifications between Admin and Users
 */

require_once 'db.php';
require_once 'send_notification.php';

/**
 * Send notification to a specific user or all users (broadcast)
 */
function sendNotificationToUser($userId, $title, $message, $data, $conn)
{
    // Get user's FCM token for push notification
    $tokenResult = $conn->query("SELECT token FROM fcm_tokens WHERE user_id = $userId");
    if ($tokenRow = $tokenResult->fetch_assoc()) {
        $notification = ['title' => $title, 'body' => $message];
        sendFCMMessage($tokenRow['token'], $notification, $data);
    }

    // Save to database for in-app notification
    $notifData = json_encode($data);
    $notifType = $data['type'] ?? 'general';
    $stmt = $conn->prepare("INSERT INTO notifications (user_id, type, title, message, data, created_at) VALUES (?, ?, ?, ?, ?, NOW())");
    $stmt->bind_param("issss", $userId, $notifType, $title, $message, $notifData);
    $stmt->execute();
    $stmt->close();
}

/**
 * Send notification to all admins
 */
function sendNotificationToAdmins($title, $message, $data, $conn)
{
    $adminResult = $conn->query("SELECT u.id, f.token FROM users u 
                                 LEFT JOIN fcm_tokens f ON u.id = f.user_id 
                                 WHERE u.user_type = 'admin'");

    while ($adminRow = $adminResult->fetch_assoc()) {
        $adminId = $adminRow['id'];

        // Send push if has token
        if ($adminRow['token']) {
            $notification = ['title' => $title, 'body' => $message];
            sendFCMMessage($adminRow['token'], $notification, $data);
        }

        // Save in-app notification
        $notifData = json_encode($data);
        $notifType = $data['type'] ?? 'general';
        $stmt = $conn->prepare("INSERT INTO notifications (user_id, type, title, message, data, created_at) VALUES (?, ?, ?, ?, ?, NOW())");
        $stmt->bind_param("issss", $adminId, $notifType, $title, $message, $notifData);
        $stmt->execute();
        $stmt->close();
    }
}

/**
 * Broadcast notification to all users
 */
function broadcastToAllUsers($title, $message, $data, $conn)
{
    $userResult = $conn->query("SELECT u.id, f.token FROM users u 
                                LEFT JOIN fcm_tokens f ON u.id = f.user_id 
                                WHERE u.user_type = 'reader'");

    while ($userRow = $userResult->fetch_assoc()) {
        $userId = $userRow['id'];

        if ($userRow['token']) {
            $notification = ['title' => $title, 'body' => $message];
            sendFCMMessage($userRow['token'], $notification, $data);
        }

        $notifData = json_encode($data);
        $notifType = $data['type'] ?? 'broadcast';
        $stmt = $conn->prepare("INSERT INTO notifications (user_id, type, title, message, data, created_at) VALUES (?, ?, ?, ?, ?, NOW())");
        $stmt->bind_param("issss", $userId, $notifType, $title, $message, $notifData);
        $stmt->execute();
        $stmt->close();
    }
}

// ============================================
// ADMIN → USER NOTIFICATIONS (12 types)
// ============================================

/**
 * 1. New book added (broadcast)
 */
function notifyNewBookAdded($bookId, $bookTitle, $author, $conn)
{
    $title = "📚 New Book Added!";
    $message = "'$bookTitle' by $author is now available.";
    $data = [
        'type' => 'new_book',
        'book_id' => strval($bookId),
        'book_title' => $bookTitle,
        'sender' => 'admin',
        'receiver' => 'all_users'
    ];
    broadcastToAllUsers($title, $message, $data, $conn);
}

/**
 * 2. Reservation approved (individual)
 */
function notifyReservationApproved($userId, $reservationId, $bookTitle, $conn)
{
    $title = "✅ Reservation Approved";
    $message = "Your reservation for '$bookTitle' has been approved!";
    $data = [
        'type' => 'reservation_approved',
        'reservation_id' => strval($reservationId),
        'book_title' => $bookTitle,
        'sender' => 'admin',
        'receiver' => 'user'
    ];
    sendNotificationToUser($userId, $title, $message, $data, $conn);
}

/**
 * 3. Reservation rejected (individual)
 */
function notifyReservationRejected($userId, $reservationId, $bookTitle, $reason, $conn)
{
    $title = "❌ Reservation Rejected";
    $message = "Your reservation for '$bookTitle' was rejected. $reason";
    $data = [
        'type' => 'reservation_rejected',
        'reservation_id' => strval($reservationId),
        'book_title' => $bookTitle,
        'reason' => $reason,
        'sender' => 'admin',
        'receiver' => 'user'
    ];
    sendNotificationToUser($userId, $title, $message, $data, $conn);
}

/**
 * 4. Book ready for pickup (individual)
 */
function notifyBookReadyForPickup($userId, $reservationId, $bookTitle, $libraryName, $conn)
{
    $title = "📦 Book Ready for Pickup";
    $message = "'$bookTitle' is ready at $libraryName. Please collect soon.";
    $data = [
        'type' => 'ready_for_pickup',
        'reservation_id' => strval($reservationId),
        'book_title' => $bookTitle,
        'library' => $libraryName,
        'sender' => 'admin',
        'receiver' => 'user'
    ];
    sendNotificationToUser($userId, $title, $message, $data, $conn);
}

/**
 * 5. Return reminder (individual)
 */
function notifyReturnReminder($userId, $bookTitle, $dueDate, $conn)
{
    $title = "⏰ Return Reminder";
    $message = "Please return '$bookTitle' by $dueDate.";
    $data = [
        'type' => 'return_reminder',
        'book_title' => $bookTitle,
        'due_date' => $dueDate,
        'sender' => 'admin',
        'receiver' => 'user'
    ];
    sendNotificationToUser($userId, $title, $message, $data, $conn);
}

/**
 * 6. Overdue fine alert (individual)
 */
function notifyOverdueFine($userId, $bookTitle, $fineAmount, $conn)
{
    $title = "⚠️ Overdue Fine Alert";
    $message = "'$bookTitle' is overdue. Fine: ₹$fineAmount";
    $data = [
        'type' => 'overdue_fine',
        'book_title' => $bookTitle,
        'fine_amount' => strval($fineAmount),
        'sender' => 'admin',
        'receiver' => 'user'
    ];
    sendNotificationToUser($userId, $title, $message, $data, $conn);
}

/**
 * 7. Account blocked/unblocked (individual)
 */
function notifyAccountStatus($userId, $status, $reason, $conn)
{
    if ($status === 'blocked') {
        $title = "🚫 Account Blocked";
        $message = "Your account has been blocked. Reason: $reason";
    } else {
        $title = "✅ Account Unblocked";
        $message = "Your account has been restored. You can now use all features.";
    }
    $data = [
        'type' => 'account_' . $status,
        'status' => $status,
        'reason' => $reason,
        'sender' => 'admin',
        'receiver' => 'user'
    ];
    sendNotificationToUser($userId, $title, $message, $data, $conn);
}

/**
 * 8. Library announcements (broadcast)
 */
function notifyLibraryAnnouncement($announcementTitle, $announcementMessage, $conn)
{
    $title = "📢 " . $announcementTitle;
    $data = [
        'type' => 'announcement',
        'sender' => 'admin',
        'receiver' => 'all_users'
    ];
    broadcastToAllUsers($title, $announcementMessage, $data, $conn);
}

/**
 * 9. Book availability alert (individual - for wishlist)
 */
function notifyBookAvailable($userId, $bookId, $bookTitle, $conn)
{
    $title = "📗 Book Now Available!";
    $message = "'$bookTitle' from your wishlist is now available.";
    $data = [
        'type' => 'book_available',
        'book_id' => strval($bookId),
        'book_title' => $bookTitle,
        'sender' => 'admin',
        'receiver' => 'user'
    ];
    sendNotificationToUser($userId, $title, $message, $data, $conn);
}

/**
 * 10. Reservation cancelled by admin (individual)
 */
function notifyReservationCancelledByAdmin($userId, $reservationId, $bookTitle, $reason, $conn)
{
    $title = "🚫 Reservation Cancelled";
    $message = "Your reservation for '$bookTitle' was cancelled. $reason";
    $data = [
        'type' => 'reservation_cancelled',
        'reservation_id' => strval($reservationId),
        'book_title' => $bookTitle,
        'cancelled_by' => 'admin',
        'reason' => $reason,
        'sender' => 'admin',
        'receiver' => 'user'
    ];
    sendNotificationToUser($userId, $title, $message, $data, $conn);
}

/**
 * 11. QR pickup generated (individual)
 */
function notifyQRPickupGenerated($userId, $reservationId, $bookTitle, $qrCode, $conn)
{
    $title = "🔳 QR Code Ready";
    $message = "Your pickup QR for '$bookTitle' is ready. Show at counter.";
    $data = [
        'type' => 'qr_pickup',
        'reservation_id' => strval($reservationId),
        'book_title' => $bookTitle,
        'qr_code' => $qrCode,
        'sender' => 'admin',
        'receiver' => 'user'
    ];
    sendNotificationToUser($userId, $title, $message, $data, $conn);
}

/**
 * 12. Feedback request (individual)
 */
function notifyFeedbackRequest($userId, $bookTitle, $reservationId, $conn)
{
    $title = "⭐ Rate Your Experience";
    $message = "How was your experience with '$bookTitle'? Please share feedback.";
    $data = [
        'type' => 'feedback_request',
        'reservation_id' => strval($reservationId),
        'book_title' => $bookTitle,
        'sender' => 'admin',
        'receiver' => 'user'
    ];
    sendNotificationToUser($userId, $title, $message, $data, $conn);
}

// ============================================
// USER → ADMIN NOTIFICATIONS (6 types)
// ============================================

/**
 * 1. New user registration
 */
function notifyNewUserRegistration($userId, $userName, $userEmail, $conn)
{
    $title = "👤 New User Registered";
    $message = "$userName ($userEmail) just registered.";
    $data = [
        'type' => 'new_registration',
        'user_id' => strval($userId),
        'user_name' => $userName,
        'user_email' => $userEmail,
        'sender' => 'user',
        'receiver' => 'admin'
    ];
    sendNotificationToAdmins($title, $message, $data, $conn);
}

/**
 * 2. New book reservation request
 */
function notifyNewReservationRequest($userId, $userName, $reservationId, $bookTitle, $pickupDate, $conn)
{
    $title = "📚 New Reservation";
    $message = "$userName reserved '$bookTitle' for $pickupDate.";
    $data = [
        'type' => 'new_reservation',
        'reservation_id' => strval($reservationId),
        'user_id' => strval($userId),
        'user_name' => $userName,
        'book_title' => $bookTitle,
        'pickup_date' => $pickupDate,
        'sender' => 'user',
        'receiver' => 'admin'
    ];
    sendNotificationToAdmins($title, $message, $data, $conn);
}

/**
 * 3. Reservation cancelled by user
 */
function notifyReservationCancelledByUser($userId, $userName, $reservationId, $bookTitle, $conn)
{
    $title = "🚫 Reservation Cancelled";
    $message = "$userName cancelled reservation for '$bookTitle'.";
    $data = [
        'type' => 'reservation_cancelled_user',
        'reservation_id' => strval($reservationId),
        'user_id' => strval($userId),
        'user_name' => $userName,
        'book_title' => $bookTitle,
        'cancelled_by' => 'user',
        'sender' => 'user',
        'receiver' => 'admin'
    ];
    sendNotificationToAdmins($title, $message, $data, $conn);
}

/**
 * 4. Book returned
 */
function notifyBookReturned($userId, $userName, $bookTitle, $reservationId, $conn)
{
    $title = "📗 Book Returned";
    $message = "$userName returned '$bookTitle'.";
    $data = [
        'type' => 'book_returned',
        'reservation_id' => strval($reservationId),
        'user_id' => strval($userId),
        'user_name' => $userName,
        'book_title' => $bookTitle,
        'sender' => 'user',
        'receiver' => 'admin'
    ];
    sendNotificationToAdmins($title, $message, $data, $conn);
}

/**
 * 5. Feedback submitted
 */
function notifyFeedbackSubmitted($userId, $userName, $bookTitle, $rating, $conn)
{
    $title = "⭐ New Feedback";
    $message = "$userName rated '$bookTitle': $rating/5 stars.";
    $data = [
        'type' => 'feedback_submitted',
        'user_id' => strval($userId),
        'user_name' => $userName,
        'book_title' => $bookTitle,
        'rating' => strval($rating),
        'sender' => 'user',
        'receiver' => 'admin'
    ];
    sendNotificationToAdmins($title, $message, $data, $conn);
}

/**
 * 6. Support request
 */
function notifySupportRequest($userId, $userName, $subject, $message, $conn)
{
    $title = "🆘 Support Request";
    $notifMessage = "$userName: $subject";
    $data = [
        'type' => 'support_request',
        'user_id' => strval($userId),
        'user_name' => $userName,
        'subject' => $subject,
        'message' => $message,
        'sender' => 'user',
        'receiver' => 'admin'
    ];
    sendNotificationToAdmins($title, $notifMessage, $data, $conn);
}

?>