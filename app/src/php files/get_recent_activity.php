<?php
/**
 * Get Recent Activity API
 * Returns recent activities for admin dashboard
 * - New reservations
 * - New user registrations
 * - Book returns
 * - Status changes
 */

require_once 'db.php';

// Set headers
setHeaders();

// Only allow GET requests
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $conn = getConnection();

    $activities = [];

    // 1. Get recent reservations (last 10)
    $reservationQuery = "
        SELECT r.id, r.created_at, r.status, b.title as book_title, u.name as user_name
        FROM reservations r
        LEFT JOIN books b ON r.book_id = b.id
        LEFT JOIN users u ON r.user_id = u.id
        ORDER BY r.created_at DESC
        LIMIT 5
    ";

    $result = $conn->query($reservationQuery);
    if ($result) {
        while ($row = $result->fetch_assoc()) {
            $activities[] = [
                'type' => 'reservation',
                'title' => 'New reservation request',
                'description' => '"' . ($row['book_title'] ?? 'Unknown Book') . '" by ' . ($row['user_name'] ?? 'Unknown User'),
                'status' => $row['status'],
                'created_at' => $row['created_at']
            ];
        }
    }

    // 2. Get recent user registrations (last 5)
    $userQuery = "
        SELECT id, name, email, created_at
        FROM users
        WHERE user_type = 'reader'
        ORDER BY created_at DESC
        LIMIT 3
    ";

    $result = $conn->query($userQuery);
    if ($result) {
        while ($row = $result->fetch_assoc()) {
            $activities[] = [
                'type' => 'user_registered',
                'title' => 'New user registered',
                'description' => $row['email'] ?? $row['name'],
                'status' => 'new',
                'created_at' => $row['created_at']
            ];
        }
    }

    // 3. Get recently added books (last 3)
    $bookQuery = "
        SELECT id, title, author, created_at
        FROM books
        WHERE is_new = 1
        ORDER BY created_at DESC
        LIMIT 3
    ";

    $result = $conn->query($bookQuery);
    if ($result) {
        while ($row = $result->fetch_assoc()) {
            $activities[] = [
                'type' => 'book_added',
                'title' => 'New book added',
                'description' => '"' . $row['title'] . '" by ' . ($row['author'] ?? 'Unknown'),
                'status' => 'new',
                'created_at' => $row['created_at']
            ];
        }
    }

    // Sort all activities by created_at descending
    usort($activities, function ($a, $b) {
        return strtotime($b['created_at']) - strtotime($a['created_at']);
    });

    // Limit to 10 most recent
    $activities = array_slice($activities, 0, 10);

    // Add relative time for each activity
    foreach ($activities as &$activity) {
        $activity['time_ago'] = getTimeAgo($activity['created_at']);
    }

    respond(true, 'Activities fetched successfully', ['activities' => $activities]);

    $conn->close();

} catch (Exception $e) {
    error_log("Get Recent Activity Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}

/**
 * Calculate relative time (e.g., "5 minutes ago")
 */
function getTimeAgo($datetime)
{
    $time = strtotime($datetime);
    $diff = time() - $time;

    if ($diff < 60) {
        return 'Just now';
    } elseif ($diff < 3600) {
        $mins = floor($diff / 60);
        return $mins . ' minute' . ($mins > 1 ? 's' : '') . ' ago';
    } elseif ($diff < 86400) {
        $hours = floor($diff / 3600);
        return $hours . ' hour' . ($hours > 1 ? 's' : '') . ' ago';
    } elseif ($diff < 604800) {
        $days = floor($diff / 86400);
        return $days . ' day' . ($days > 1 ? 's' : '') . ' ago';
    } else {
        return date('M j, Y', $time);
    }
}
?>