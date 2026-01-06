<?php
/**
 * Create transactions table
 */
require_once 'db.php';

try {
    $conn = getConnection();

    $sql = "CREATE TABLE IF NOT EXISTS transactions (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id INT NOT NULL,
        reservation_id INT,
        book_id INT,
        type ENUM('deposit', 'refund', 'fine', 'pending_fine') NOT NULL,
        amount DECIMAL(10,2) NOT NULL,
        status ENUM('completed', 'pending', 'cancelled') DEFAULT 'completed',
        reason VARCHAR(50) COMMENT 'safe, late, damaged, lost, booking, rejected',
        days_late INT DEFAULT 0,
        fine_rate DECIMAL(10,2) DEFAULT 0,
        notes TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        processed_at TIMESTAMP NULL,
        INDEX idx_type (type),
        INDEX idx_status (status),
        INDEX idx_user (user_id),
        INDEX idx_created (created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    if ($conn->query($sql)) {
        echo "SUCCESS: transactions table created/exists!\n";
    } else {
        echo "ERROR: " . $conn->error . "\n";
    }

    // Show structure
    $result = $conn->query("DESCRIBE transactions");
    echo "\nTable structure:\n";
    while ($row = $result->fetch_assoc()) {
        echo "- " . $row['Field'] . " (" . $row['Type'] . ")\n";
    }

    $conn->close();
} catch (Exception $e) {
    echo "Error: " . $e->getMessage();
}
?>