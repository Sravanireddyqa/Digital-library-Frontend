<?php
/**
 * Update Profile API
 * Updates user name and phone number
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $data = json_decode(file_get_contents('php://input'), true);

    if (!isset($data['user_id'])) {
        respond(false, 'User ID is required');
    }

    $userId = intval($data['user_id']);
    $name = isset($data['name']) ? trim($data['name']) : null;
    $phone = isset($data['phone']) ? trim($data['phone']) : null;

    $conn = getConnection();

    // Check if phone column exists, add if not
    $checkPhone = $conn->query("SHOW COLUMNS FROM users LIKE 'phone'");
    $phoneColumnExists = $checkPhone->num_rows > 0;

    if (!$phoneColumnExists && $phone !== null) {
        // Add phone column if it doesn't exist
        $conn->query("ALTER TABLE users ADD COLUMN phone VARCHAR(20) DEFAULT NULL");
        $phoneColumnExists = true;
    }

    // Build update query dynamically
    $updates = [];
    $params = [];
    $types = '';

    if ($name !== null && !empty($name)) {
        $updates[] = 'name = ?';
        $params[] = $name;
        $types .= 's';
    }

    if ($phone !== null && $phoneColumnExists) {
        $updates[] = 'phone = ?';
        $params[] = $phone;
        $types .= 's';
    }

    if (empty($updates)) {
        respond(false, 'No fields to update');
    }

    $params[] = $userId;
    $types .= 'i';

    $sql = "UPDATE users SET " . implode(', ', $updates) . " WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param($types, ...$params);

    if ($stmt->execute()) {
        // Get updated user data
        $selectColumns = "id, name, email";
        if ($phoneColumnExists) {
            $selectColumns .= ", phone";
        }

        $getUser = $conn->prepare("SELECT $selectColumns FROM users WHERE id = ?");
        $getUser->bind_param("i", $userId);
        $getUser->execute();
        $user = $getUser->get_result()->fetch_assoc();
        $getUser->close();

        respond(true, 'Profile updated successfully', ['user' => $user]);
    } else {
        respond(false, 'Failed to update profile: ' . $conn->error);
    }

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    error_log("Update Profile Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}
?>