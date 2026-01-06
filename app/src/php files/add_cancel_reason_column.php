<?php
/**
 * Add cancel_reason column to reservations table
 */

require_once 'db.php';

setHeaders();

try {
    $conn = getConnection();

    // Check if column exists
    $columnCheck = $conn->query("SHOW COLUMNS FROM reservations LIKE 'cancel_reason'");

    if ($columnCheck->num_rows == 0) {
        // Add the column
        $result = $conn->query("ALTER TABLE reservations ADD COLUMN cancel_reason TEXT DEFAULT NULL");
        if ($result) {
            respond(true, 'cancel_reason column added successfully');
        } else {
            respond(false, 'Failed to add column: ' . $conn->error);
        }
    } else {
        respond(true, 'cancel_reason column already exists');
    }

    // Show current cancel reasons
    $reasons = $conn->query("SELECT id, status, cancel_reason FROM reservations WHERE status = 'cancelled' ORDER BY id DESC LIMIT 10");
    $data = [];
    while ($row = $reasons->fetch_assoc()) {
        $data[] = $row;
    }

    echo "\n\nCancelled reservations with reasons:\n";
    print_r($data);

    $conn->close();

} catch (Exception $e) {
    respond(false, 'Error: ' . $e->getMessage());
}
?>