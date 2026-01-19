-- Create transactions table for Payments & Refunds
CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    reservation_id INT,
    book_id INT,
    type ENUM('deposit', 'refund', 'fine', 'pending_fine') NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('completed', 'pending', 'cancelled') DEFAULT 'completed',
    reason VARCHAR(50) COMMENT 'safe_return, late, damaged, lost, rejected, booking',
    days_late INT DEFAULT 0,
    fine_rate DECIMAL(10,2) DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE SET NULL,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE SET NULL,
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_user (user_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- View table structure
DESCRIBE transactions;
