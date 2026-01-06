<?php
/**
 * Update Invoice API
 * Updates invoice status (mark as paid)
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $data = json_decode(file_get_contents('php://input'), true);

    if (!isset($data['invoice_id']) || !isset($data['status'])) {
        respond(false, 'Missing invoice_id or status');
    }

    $invoiceId = intval($data['invoice_id']);
    $status = strtolower(trim($data['status']));

    if (!in_array($status, ['paid', 'unpaid', 'overdue'])) {
        respond(false, 'Invalid status. Use: paid, unpaid, overdue');
    }

    $conn = getConnection();

    // Get invoice details with user info
    $checkStmt = $conn->prepare("SELECT i.*, u.name as user_name, r.book_id, b.title as book_title 
                                  FROM invoices i 
                                  LEFT JOIN users u ON i.user_id = u.id 
                                  LEFT JOIN reservations r ON i.reservation_id = r.id 
                                  LEFT JOIN books b ON r.book_id = b.id 
                                  WHERE i.id = ?");
    $checkStmt->bind_param("i", $invoiceId);
    $checkStmt->execute();
    $result = $checkStmt->get_result();
    $invoice = $result->fetch_assoc();
    $checkStmt->close();

    if (!$invoice) {
        respond(false, 'Invoice not found');
    }

    $userId = $invoice['user_id'];
    $userName = $invoice['user_name'] ?? 'User';
    $bookTitle = $invoice['book_title'] ?? 'Book';
    $amount = $invoice['amount'] ?? 0;

    // Update invoice status
    $stmt = $conn->prepare("UPDATE invoices SET status = ? WHERE id = ?");
    $stmt->bind_param("si", $status, $invoiceId);

    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            // Send notifications
            require_once 'send_notification.php';

            // Notification based on status
            if ($status === 'paid') {
                // REFUND/PAYMENT notification to user
                $userNotification = [
                    'title' => "💰 Payment Confirmed",
                    'body' => "Your deposit of ₹$amount for '$bookTitle' has been refunded/settled."
                ];
                $notifType = 'refund_processed';
            } elseif ($status === 'overdue') {
                $userNotification = [
                    'title' => "⚠️ Payment Overdue",
                    'body' => "Your deposit for '$bookTitle' is overdue. Please contact library."
                ];
                $notifType = 'payment_overdue';
            } else {
                $userNotification = [
                    'title' => "📋 Invoice Updated",
                    'body' => "Invoice for '$bookTitle' has been updated."
                ];
                $notifType = 'invoice_updated';
            }

            $userData = [
                'type' => $notifType,
                'invoice_id' => strval($invoiceId),
                'book_title' => $bookTitle,
                'amount' => strval($amount)
            ];

            // Send push notification to user
            $tokenResult = $conn->query("SELECT token FROM fcm_tokens WHERE user_id = $userId");
            if ($tokenRow = $tokenResult->fetch_assoc()) {
                sendFCMMessage($tokenRow['token'], $userNotification, $userData);
            }

            // Save in-app notification for user
            $notifTitle = $userNotification['title'];
            $notifMessage = $userNotification['body'];
            $notifData = json_encode($userData);
            $stmt2 = $conn->prepare("INSERT INTO notifications (user_id, type, title, message, data, created_at) VALUES (?, ?, ?, ?, ?, NOW())");
            $stmt2->bind_param("issss", $userId, $notifType, $notifTitle, $notifMessage, $notifData);
            $stmt2->execute();
            $stmt2->close();

            // Notify all admins
            $adminNotification = [
                'title' => "💰 Invoice " . ucfirst($status),
                'body' => "$userName's invoice for '$bookTitle' marked as $status"
            ];
            $adminData = [
                'type' => 'invoice_' . $status,
                'invoice_id' => strval($invoiceId),
                'user_name' => $userName,
                'book_title' => $bookTitle
            ];

            $adminResult = $conn->query("SELECT u.id, f.token FROM users u 
                                         LEFT JOIN fcm_tokens f ON u.id = f.user_id 
                                         WHERE u.role = 'admin'");

            while ($adminRow = $adminResult->fetch_assoc()) {
                $adminId = $adminRow['id'];
                if ($adminRow['token']) {
                    sendFCMMessage($adminRow['token'], $adminNotification, $adminData);
                }
                $adminNotifTitle = $adminNotification['title'];
                $adminNotifMessage = $adminNotification['body'];
                $adminNotifData = json_encode($adminData);
                $adminNotifType = 'invoice_' . $status;
                $stmt3 = $conn->prepare("INSERT INTO notifications (user_id, type, title, message, data, created_at) VALUES (?, ?, ?, ?, ?, NOW())");
                $stmt3->bind_param("issss", $adminId, $adminNotifType, $adminNotifTitle, $adminNotifMessage, $adminNotifData);
                $stmt3->execute();
                $stmt3->close();
            }

            respond(true, 'Invoice updated to ' . $status);
        } else {
            respond(false, 'Invoice not found or status unchanged');
        }
    } else {
        respond(false, 'Failed to update invoice: ' . $conn->error);
    }

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    error_log("Update Invoice Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>