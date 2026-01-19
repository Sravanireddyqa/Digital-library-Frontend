-- Fix stock values for books showing "Out of stock"
-- Run this in phpMyAdmin

-- Set default stock of 10 for all books that have 0 stock
UPDATE books SET stock = 10 WHERE stock = 0 OR stock IS NULL;

-- Verify the fix
SELECT id, title, stock FROM books WHERE stock <= 0;
