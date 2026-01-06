<?php
/**
 * Get Invoices API
 * Returns list of all invoices/fines
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $conn = getConnection();

    // Check if invoices table exists, create if not
    $tableCheck = $conn->query("SHOW TABLES LIKE 'invoices'");
    if ($tableCheck->num_rows == 0) {
        $createTable = "CREATE TABLE invoices (
            id INT AUTO_INCREMENT PRIMARY KEY,
            invoice_id VARCHAR(50) NOT NULL,
            user_id INT NOT NULL,
            book_id INT DEFAULT NULL,
            reason VARCHAR(100) NOT NULL,
            amount DECIMAL(10,2) NOT NULL,
            status ENUM('paid', 'unpaid', 'overdue') DEFAULT 'unpaid',
            date DATE DEFAULT CURRENT_DATE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        )";
        $conn->query($createTable);

        // Add sample invoices
        $conn->query("INSERT INTO invoices (invoice_id, user_id, book_id, reason, amount, status, date) VALUES
            ('INV-2024001', 1, 1, 'Late Return', 50.00, 'unpaid', CURDATE()),
            ('INV-2024002', 2, 2, 'Damaged Pages', 100.00, 'paid', DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
            ('INV-2024003', 1, 3, 'Lost Book', 500.00, 'overdue', DATE_SUB(CURDATE(), INTERVAL 30 DAY)),
            ('INV-2024004', 3, NULL, 'Membership Fee', 200.00, 'paid', DATE_SUB(CURDATE(), INTERVAL 10 DAY)),
            ('INV-2024005', 2, 4, 'Late Return', 25.00, 'unpaid', DATE_SUB(CURDATE(), INTERVAL 2 DAY))
        ");
    }

    // Get all invoices with user and book info
    $sql = "SELECT 
                i.id,
                i.invoice_id,
                COALESCE(u.name, 'Unknown User') as user_name,
                COALESCE(u.email, '') as user_email,
                COALESCE(b.title, 'N/A') as book_title,
                i.reason,
                i.amount,
                i.status,
                DATE_FORMAT(i.date, '%b %d, %Y') as date
            FROM invoices i
            LEFT JOIN users u ON i.user_id = u.id
            LEFT JOIN books b ON i.book_id = b.id
            ORDER BY i.id DESC";

    $result = $conn->query($sql);

    if (!$result) {
        respond(false, 'Query failed: ' . $conn->error);
    }

    $invoices = [];
    while ($row = $result->fetch_assoc()) {
        $invoices[] = [
            'id' => (int) $row['id'],
            'invoice_id' => $row['invoice_id'],
            'user_name' => $row['user_name'],
            'user_email' => $row['user_email'],
            'book_title' => $row['book_title'],
            'reason' => $row['reason'],
            'amount' => (float) $row['amount'],
            'status' => $row['status'],
            'date' => $row['date']
        ];
    }

    respond(true, 'Invoices fetched successfully', [
        'invoices' => $invoices,
        'count' => count($invoices)
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("Get Invoices Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>