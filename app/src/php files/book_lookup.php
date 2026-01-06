<?php
/**
 * Book/Reservation Lookup API
 * ============================
 * Handles both book ISBN lookup and reservation QR code lookup
 * 
 * GET /book_lookup.php?qr_code=RES-001-2024  (Reservation lookup)
 * GET /book_lookup.php?qr_code=978-81-4672   (Book ISBN lookup)
 * POST /book_lookup.php { "barcode": "978-81-4672" }
 */

// Suppress PHP warnings that break JSON output
error_reporting(0);
ini_set('display_errors', 0);

require_once 'db.php';
setHeaders();

// Get input from either GET or POST
$qrCode = null;

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $qrCode = isset($_GET['qr_code']) ? trim($_GET['qr_code']) : null;
    if (!$qrCode) {
        $qrCode = isset($_GET['barcode']) ? trim($_GET['barcode']) : null;
    }
} else if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = getInput();
    $qrCode = isset($input['qr_code']) ? trim($input['qr_code']) : null;
    if (!$qrCode) {
        $qrCode = isset($input['barcode']) ? trim($input['barcode']) : null;
    }
}

if (!$qrCode) {
    http_response_code(400);
    respond(false, 'QR code or barcode is required');
}

$conn = getConnection();

// Check if it's a reservation QR code (format: RES-XXX-YYYY or LibraryAI|RES-XXX...)
if (preg_match('/RES-\d{3}-\d{4}/', $qrCode, $matches)) {
    $reservationCode = $matches[0];

    // Extract reservation ID from code (e.g., RES-001-2024 -> 1)
    $parts = explode('-', $reservationCode);
    $reservationId = intval($parts[1]);

    // Look up reservation with book details - check which date column exists
    $stmt = $conn->prepare("
        SELECT r.*, b.title as book_title, b.author, b.isbn, b.category, b.cover_url, b.stock,
               u.name as user_name, u.email as user_email
        FROM reservations r
        JOIN books b ON r.book_id = b.id
        JOIN users u ON r.user_id = u.id
        WHERE r.id = ?
        LIMIT 1
    ");
    $stmt->bind_param("i", $reservationId);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $reservation = $result->fetch_assoc();

        // Get date from whichever column exists
        $reservationDate = isset($reservation['reservation_date']) ? $reservation['reservation_date'] :
            (isset($reservation['date']) ? $reservation['date'] :
                (isset($reservation['created_at']) ? $reservation['created_at'] : null));

        // Return both reservation and book data
        respond(true, 'Reservation found', [
            'type' => 'reservation',
            'reservation' => [
                'id' => $reservation['id'],
                'status' => $reservation['status'],
                'reservation_date' => $reservationDate,
                'time_slot' => isset($reservation['time_slot']) ? $reservation['time_slot'] : '',
                'user_name' => $reservation['user_name'],
                'user_email' => $reservation['user_email']
            ],
            'book' => [
                'id' => $reservation['book_id'],
                'title' => $reservation['book_title'],
                'author' => $reservation['author'],
                'isbn' => isset($reservation['isbn']) ? $reservation['isbn'] : '',
                'category' => isset($reservation['category']) ? $reservation['category'] : '',
                'cover_url' => isset($reservation['cover_url']) ? $reservation['cover_url'] : '',
                'stock' => isset($reservation['stock']) ? $reservation['stock'] : 0
            ]
        ]);
    } else {
        http_response_code(404);
        respond(false, 'Reservation not found');
    }

} else {
    // Regular book lookup by ISBN or ID
    $stmt = $conn->prepare("SELECT id, title, author, isbn, category, copies, stock, cover_url FROM books WHERE isbn = ? OR id = ? LIMIT 1");
    $stmt->bind_param("ss", $qrCode, $qrCode);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $book = $result->fetch_assoc();
        respond(true, 'Book found', [
            'type' => 'book',
            'book' => $book
        ]);
    } else {
        http_response_code(404);
        respond(false, 'Book not found');
    }
}

$conn->close();
?>