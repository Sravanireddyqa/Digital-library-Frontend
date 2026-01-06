-- =====================================================
-- Digital Library - Notifications Database Schema
-- =====================================================
-- Run this SQL in your phpMyAdmin or MySQL client
-- Database: digitallibrary
-- =====================================================

USE digitallibrary;

-- =====================================================
-- TABLE: notifications
-- Stores all user notifications
-- =====================================================
CREATE TABLE IF NOT EXISTS `notifications` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `type` VARCHAR(50) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `message` TEXT NOT NULL,
    `data` JSON DEFAULT NULL,
    `is_read` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_is_read` (`is_read`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLE: fcm_tokens
-- Stores Firebase Cloud Messaging tokens for push notifications
-- =====================================================
CREATE TABLE IF NOT EXISTS `fcm_tokens` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `token` VARCHAR(255) NOT NULL UNIQUE,
    `device_info` VARCHAR(255) DEFAULT NULL,
    `last_updated` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_token` (`token`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Sample Data (Optional - For Testing)
-- =====================================================

-- Insert a sample notification for user ID 1 (if exists)
-- INSERT INTO `notifications` (`user_id`, `type`, `title`, `message`, `data`)
-- VALUES 
-- (1, 'general_announcement', '📢 Welcome!', 'Welcome to Digital Library notification system!', NULL),
-- (1, 'new_book', '📚 New Book Added', 'Check out our latest addition: Data Structures Made Easy', '{"book_id": 1}');

-- =====================================================
-- Queries for Testing
-- =====================================================

-- View all notifications
-- SELECT * FROM notifications ORDER BY created_at DESC;

-- View all FCM tokens
-- SELECT * FROM fcm_tokens;

-- Count unread notifications by user
-- SELECT user_id, COUNT(*) as unread_count 
-- FROM notifications 
-- WHERE is_read = FALSE 
-- GROUP BY user_id;

-- Delete old read notifications (older than 30 days)
-- DELETE FROM notifications 
-- WHERE is_read = TRUE 
-- AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
