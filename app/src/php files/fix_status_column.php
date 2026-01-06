<?php
/**
 * Check and fix status column in reservations table
 * Run this script once to ensure 'returned' status is allowed
 */

require_once 'db.php';

try {
    $conn = getConnection();

    // Check current column type
    $result = $conn->query("SHOW COLUMNS FROM reservations LIKE 'status'");
    $column = $result->fetch_assoc();

    echo "Current status column type: " . $column['Type'] . "\n";

    // If it's an ENUM, we need to alter it to include 'returned'
    if (strpos(strtolower($column['Type']), 'enum') !== false) {
        echo "Status column is ENUM. Checking if 'returned' is allowed...\n";

        // Check if 'returned' is in the ENUM values
        if (strpos(strtolower($column['Type']), 'returned') === false) {
            echo "Adding 'returned' to ENUM values...\n";

            // Alter the column to include 'returned'
            $alterQuery = "ALTER TABLE reservations MODIFY COLUMN status ENUM('pending', 'approved', 'rejected', 'returned') DEFAULT 'pending'";
            if ($conn->query($alterQuery)) {
                echo "SUCCESS: 'returned' status added to ENUM values!\n";
            } else {
                echo "ERROR: Failed to alter column - " . $conn->error . "\n";
            }
        } else {
            echo "'returned' is already in ENUM values.\n";
        }
    } else {
        echo "Status column is VARCHAR/TEXT, no ENUM restriction.\n";
    }

    // Check current reservations with their status
    echo "\nCurrent reservation statuses:\n";
    $result = $conn->query("SELECT id, status FROM reservations ORDER BY id DESC LIMIT 10");
    while ($row = $result->fetch_assoc()) {
        echo "ID: " . $row['id'] . " - Status: " . $row['status'] . "\n";
    }

    $conn->close();

} catch (Exception $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
?>