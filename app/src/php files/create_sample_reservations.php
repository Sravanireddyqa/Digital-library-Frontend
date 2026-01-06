<?php
/**
 * Create Sample Reservations
 * Run this once to add test data
 * Access via browser: http://YOUR_IP/digitallibrary_API/create_sample_reservations.php
 */

require_once 'db.php';

setHeaders();

try {
    $conn = getConnection();

    // Create reservations table if not exists
    $conn->query("CREATE TABLE IF NOT EXISTS reservations (
        id INT AUTO_INCREMENT PRIMARY KEY,
        reservation_id VARCHAR(50),
        book_id INT,
        user_id INT,
        library VARCHAR(255) DEFAULT 'Central Library',
        pickup_date VARCHAR(50),
        pickup_time VARCHAR(50),
        status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
        requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    )");

    // Clear existing sample data
    $conn->query("DELETE FROM reservations");

    // Get some books for reservations
    $booksResult = $conn->query("SELECT id, title FROM books LIMIT 5");
    $books = [];
    while ($row = $booksResult->fetch_assoc()) {
        $books[] = $row;
    }

    // Get some users
    $usersResult = $conn->query("SELECT id, name, email FROM users LIMIT 3");
    $users = [];
    while ($row = $usersResult->fetch_assoc()) {
        $users[] = $row;
    }

    // If no books or users, create sample data
    if (empty($books)) {
        $books = [
            ['id' => 1, 'title' => 'The God of Small Things'],
            ['id' => 2, 'title' => 'Midnight\'s Children'],
            ['id' => 3, 'title' => 'A Suitable Boy']
        ];
    }

    if (empty($users)) {
        $users = [
            ['id' => 1, 'name' => 'John Doe', 'email' => 'john@example.com'],
            ['id' => 2, 'name' => 'Jane Smith', 'email' => 'jane@example.com'],
            ['id' => 3, 'name' => 'Bob Wilson', 'email' => 'bob@example.com']
        ];
    }

    // Sample reservation data
    $reservations = [
        [
            'reservation_id' => 'RES-001',
            'book_id' => $books[0]['id'],
            'user_id' => $users[0]['id'],
            'library' => 'Central Delhi Public Library',
            'pickup_date' => '25/1/2024',
            'pickup_time' => '10:00 AM - 12:00 PM',
            'status' => 'pending',
            'requested_at' => '2025-12-15 12:33:13'
        ],
        [
            'reservation_id' => 'RES-002',
            'book_id' => isset($books[1]) ? $books[1]['id'] : $books[0]['id'],
            'user_id' => isset($users[1]) ? $users[1]['id'] : $users[0]['id'],
            'library' => 'University Library',
            'pickup_date' => '26/1/2024',
            'pickup_time' => '2:00 PM - 4:00 PM',
            'status' => 'pending',
            'requested_at' => '2025-12-16 10:15:00'
        ],
        [
            'reservation_id' => 'RES-003',
            'book_id' => isset($books[2]) ? $books[2]['id'] : $books[0]['id'],
            'user_id' => isset($users[2]) ? $users[2]['id'] : $users[0]['id'],
            'library' => 'City Public Library',
            'pickup_date' => '27/1/2024',
            'pickup_time' => '11:00 AM - 1:00 PM',
            'status' => 'approved',
            'requested_at' => '2025-12-14 09:45:00'
        ]
    ];

    // Insert sample reservations
    $stmt = $conn->prepare("INSERT INTO reservations (reservation_id, book_id, user_id, library, pickup_date, pickup_time, status, requested_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

    $count = 0;
    foreach ($reservations as $r) {
        $stmt->bind_param(
            "siisssss",
            $r['reservation_id'],
            $r['book_id'],
            $r['user_id'],
            $r['library'],
            $r['pickup_date'],
            $r['pickup_time'],
            $r['status'],
            $r['requested_at']
        );
        if ($stmt->execute()) {
            $count++;
        }
    }

    respond(true, "Created $count sample reservations", [
        'reservations_created' => $count,
        'books_used' => count($books),
        'users_used' => count($users)
    ]);

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    respond(false, 'Error: ' . $e->getMessage());
}
?>