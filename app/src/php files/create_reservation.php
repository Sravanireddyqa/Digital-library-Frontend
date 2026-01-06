<?php
/**
 * Create Reservation API
 * Creates a new book reservation for a user with library, date, and time slot
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $data = json_decode(file_get_contents('php://input'), true);

    if (!isset($data['user_id']) || !isset($data['book_id'])) {
        respond(false, 'Missing user_id or book_id');
    }

    $userId = intval($data['user_id']);
    $bookId = intval($data['book_id']);
    $libraryId = isset($data['library_id']) ? intval($data['library_id']) : 1;
    $pickupDate = isset($data['date']) ? $data['date'] : date('Y-m-d', strtotime('+3 days'));
    $timeSlot = isset($data['time_slot']) ? $data['time_slot'] : '10:00 AM - 12:00 PM';
    // Due date set by user for reminders
    $dueDate = isset($data['due_date']) ? $data['due_date'] : date('Y-m-d', strtotime($pickupDate . ' +7 days'));

    $conn = getConnection();

    // Check if book exists (use stock column if available, otherwise skip check)
    $checkBook = $conn->prepare("SELECT id, title, stock FROM books WHERE id = ?");
    $checkBook->bind_param("i", $bookId);
    $checkBook->execute();
    $bookResult = $checkBook->get_result()->fetch_assoc();
    $checkBook->close();

    if (!$bookResult) {
        respond(false, 'Book not found');
    }

    // Check stock if column exists
    if (isset($bookResult['stock']) && $bookResult['stock'] <= 0) {
        respond(false, 'Book is not available for reservation');
    }

    // Check for existing pending reservation
    $checkExisting = $conn->prepare("SELECT id FROM reservations 
                                     WHERE user_id = ? AND book_id = ? AND status = 'pending'");
    $checkExisting->bind_param("ii", $userId, $bookId);
    $checkExisting->execute();
    if ($checkExisting->get_result()->num_rows > 0) {
        $checkExisting->close();
        respond(false, 'You already have a pending reservation for this book');
    }
    $checkExisting->close();

    // Check if reservations table has library_id and time_slot columns
    $columnsResult = $conn->query("SHOW COLUMNS FROM reservations");
    $columns = [];
    while ($col = $columnsResult->fetch_assoc()) {
        $columns[] = $col['Field'];
    }

    $hasLibraryId = in_array('library_id', $columns);
    $hasTimeSlot = in_array('time_slot', $columns);
    $hasPickupDate = in_array('pickup_date', $columns);
    $hasReservationDate = in_array('reservation_date', $columns);
    $hasDueDate = in_array('due_date', $columns);

    // Add missing columns if needed
    if (!$hasLibraryId) {
        $conn->query("ALTER TABLE reservations ADD COLUMN library_id INT DEFAULT 1");
    }
    if (!$hasTimeSlot) {
        $conn->query("ALTER TABLE reservations ADD COLUMN time_slot VARCHAR(50) DEFAULT '10:00 AM - 12:00 PM'");
    }
    if (!$hasPickupDate && !$hasReservationDate) {
        $conn->query("ALTER TABLE reservations ADD COLUMN reservation_date DATE");
    }
    if (!$hasDueDate) {
        $conn->query("ALTER TABLE reservations ADD COLUMN due_date DATE DEFAULT NULL");
    }

    // Determine the date column name
    $dateColumn = $hasPickupDate ? 'pickup_date' : 'reservation_date';
    if (!$hasPickupDate && !$hasReservationDate) {
        $dateColumn = 'reservation_date';
    }

    // Create reservation with due_date
    $sql = "INSERT INTO reservations (user_id, book_id, library_id, $dateColumn, time_slot, due_date, status, created_at) 
            VALUES (?, ?, ?, ?, ?, ?, 'pending', NOW())";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("iiisss", $userId, $bookId, $libraryId, $pickupDate, $timeSlot, $dueDate);

    if ($stmt->execute()) {
        $reservationId = $stmt->insert_id;

        // Decrease stock if column exists
        $conn->query("UPDATE books SET stock = stock - 1 WHERE id = $bookId AND stock > 0");

        // Send push notification to user
        require_once 'send_notification.php';

        $bookTitle = $bookResult['title'];

        // Notification payload for FCM
        $notification = [
            'title' => "✅ Reservation Confirmed",
            'body' => "Your reservation for '$bookTitle' has been confirmed! Pickup: $pickupDate $timeSlot"
        ];

        $data = [
            'type' => 'reservation_confirmed',
            'reservation_id' => strval($reservationId),
            'book_id' => strval($bookId),
            'book_title' => $bookTitle,
            'pickup_date' => $pickupDate,
            'time_slot' => $timeSlot
        ];

        // Get user's FCM token and send push notification
        $tokenResult = $conn->query("SELECT token FROM fcm_tokens WHERE user_id = $userId");
        if ($tokenRow = $tokenResult->fetch_assoc()) {
            sendFCMMessage($tokenRow['token'], $notification, $data);
        }

        // Save notification to database for user's in-app display
        $notifTitle = $notification['title'];
        $notifMessage = $notification['body'];
        $notifData = json_encode($data);
        $notifType = 'reservation_confirmed';
        $stmt2 = $conn->prepare("INSERT INTO notifications (user_id, type, title, message, data, created_at) VALUES (?, ?, ?, ?, ?, NOW())");
        $stmt2->bind_param("issss", $userId, $notifType, $notifTitle, $notifMessage, $notifData);
        $stmt2->execute();
        $stmt2->close();

        // ============================================
        // SEND NOTIFICATION TO ALL ADMINS
        // ============================================

        // Get username for admin notification
        $userResult = $conn->query("SELECT name FROM users WHERE id = $userId");
        $userName = "User";
        if ($userRow = $userResult->fetch_assoc()) {
            $userName = $userRow['name'];
        }

        // Admin notification content
        $adminNotification = [
            'title' => "📚 New Reservation",
            'body' => "$userName reserved '$bookTitle' for $pickupDate"
        ];

        $adminData = [
            'type' => 'new_reservation',
            'reservation_id' => strval($reservationId),
            'book_id' => strval($bookId),
            'book_title' => $bookTitle,
            'user_id' => strval($userId),
            'user_name' => $userName,
            'pickup_date' => $pickupDate,
            'time_slot' => $timeSlot
        ];

        // Get all admin users and their FCM tokens
        $adminResult = $conn->query("SELECT u.id, f.token FROM users u 
                                     LEFT JOIN fcm_tokens f ON u.id = f.user_id 
                                     WHERE u.user_type = 'admin'");

        while ($adminRow = $adminResult->fetch_assoc()) {
            $adminId = $adminRow['id'];

            // Send push notification if admin has token
            if ($adminRow['token']) {
                sendFCMMessage($adminRow['token'], $adminNotification, $adminData);
            }

            // Save in-app notification for admin
            $adminNotifTitle = $adminNotification['title'];
            $adminNotifMessage = $adminNotification['body'];
            $adminNotifData = json_encode($adminData);
            $adminNotifType = 'new_reservation';
            $stmt3 = $conn->prepare("INSERT INTO notifications (user_id, type, title, message, data, created_at) VALUES (?, ?, ?, ?, ?, NOW())");
            $stmt3->bind_param("issss", $adminId, $adminNotifType, $adminNotifTitle, $adminNotifMessage, $adminNotifData);
            $stmt3->execute();
            $stmt3->close();
        }

        respond(true, 'Book reserved successfully', [
            'reservation_id' => $reservationId,
            'pickup_date' => $pickupDate,
            'time_slot' => $timeSlot
        ]);
    } else {
        respond(false, 'Failed to create reservation: ' . $conn->error);
    }

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    error_log("Create Reservation Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>