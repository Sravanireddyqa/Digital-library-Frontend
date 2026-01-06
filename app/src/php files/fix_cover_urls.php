<?php
/**
 * Fix Cover URLs - Update localhost to server IP
 * Run once to fix existing book cover URLs in database
 */

require_once 'db.php';

setHeaders();

try {
    $conn = getConnection();

    // Get the server IP from the request or configuration
    $serverIP = $_SERVER['SERVER_ADDR'] ?? '10.36.207.135';
    $serverPort = $_SERVER['SERVER_PORT'] ?? '80';

    // If running locally, use provided IP
    if ($serverIP == '127.0.0.1' || $serverIP == '::1') {
        $serverIP = '10.36.207.135'; // Your LAN IP
    }

    $baseUrl = ($serverPort == '443' ? 'https' : 'http') . '://' . $serverIP;
    if ($serverPort != '80' && $serverPort != '443') {
        $baseUrl .= ':' . $serverPort;
    }
    $baseUrl .= '/digitallibrary_API/';

    // Update all localhost URLs in books table
    $updateSql = "UPDATE books SET cover_url = REPLACE(cover_url, 'http://localhost/digitallibrary_API/', ?) 
                  WHERE cover_url LIKE '%localhost%'";
    $stmt = $conn->prepare($updateSql);
    $stmt->bind_param("s", $baseUrl);
    $stmt->execute();
    $affectedBooks = $stmt->affected_rows;

    // Also fix 127.0.0.1 URLs
    $updateSql2 = "UPDATE books SET cover_url = REPLACE(cover_url, 'http://127.0.0.1/digitallibrary_API/', ?) 
                   WHERE cover_url LIKE '%127.0.0.1%'";
    $stmt2 = $conn->prepare($updateSql2);
    $stmt2->bind_param("s", $baseUrl);
    $stmt2->execute();
    $affectedBooks += $stmt2->affected_rows;

    respond(true, 'Cover URLs fixed successfully', [
        'affected_books' => $affectedBooks,
        'new_base_url' => $baseUrl
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("Fix Cover URLs Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>