<?php
/**
 * Remove Duplicate Books Script
 * Run this once to clean up duplicate entries
 */

require_once 'db.php';

setHeaders();

try {
    $conn = getConnection();

    // Find duplicates based on title (case insensitive)
    $duplicateQuery = "
        SELECT title, COUNT(*) as count, GROUP_CONCAT(id ORDER BY id) as ids
        FROM books 
        GROUP BY LOWER(title)
        HAVING COUNT(*) > 1
    ";

    $result = $conn->query($duplicateQuery);

    $duplicates = [];
    $deletedCount = 0;

    while ($row = $result->fetch_assoc()) {
        $ids = explode(',', $row['ids']);
        $keepId = $ids[0]; // Keep the first (oldest) entry
        $deleteIds = array_slice($ids, 1); // Delete the rest

        $duplicates[] = [
            'title' => $row['title'],
            'kept_id' => $keepId,
            'deleted_ids' => $deleteIds
        ];

        // Delete duplicates
        foreach ($deleteIds as $id) {
            $deleteStmt = $conn->prepare("DELETE FROM books WHERE id = ?");
            $deleteStmt->bind_param("i", $id);
            $deleteStmt->execute();
            $deletedCount++;
            $deleteStmt->close();
        }
    }

    // Also fix any books with rating 0 by setting them to null or removing
    // Update books with title similar to existing books
    $conn->query("DELETE FROM books WHERE title LIKE '%Automatic%Habits%' AND rating = 0");

    respond(true, 'Duplicates removed successfully', [
        'deleted_count' => $deletedCount,
        'duplicates_found' => $duplicates
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("Remove Duplicates Error: " . $e->getMessage());
    respond(false, 'Error: ' . $e->getMessage());
}
?>