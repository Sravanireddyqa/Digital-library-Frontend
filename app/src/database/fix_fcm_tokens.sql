-- Run this in phpMyAdmin to fix fcm_tokens table

-- Add updated_at column if missing
ALTER TABLE fcm_tokens ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Also add created_at if missing
ALTER TABLE fcm_tokens ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Verify the table structure
DESCRIBE fcm_tokens;
