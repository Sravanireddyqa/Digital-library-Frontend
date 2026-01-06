<?php
/**
 * Get Libraries API
 * Returns list of libraries, optionally sorted by distance
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $userLat = isset($_GET['lat']) ? floatval($_GET['lat']) : 0;
    $userLng = isset($_GET['lng']) ? floatval($_GET['lng']) : 0;

    $conn = getConnection();

    // Check if libraries table exists
    $tableCheck = $conn->query("SHOW TABLES LIKE 'libraries'");
    if ($tableCheck->num_rows == 0) {
        // Create libraries table
        $createTable = "CREATE TABLE libraries (
            id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            location VARCHAR(255),
            address TEXT,
            phone VARCHAR(20),
            email VARCHAR(100),
            opening_hours VARCHAR(50) DEFAULT '9 AM - 6 PM',
            latitude DOUBLE DEFAULT 0,
            longitude DOUBLE DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )";
        $conn->query($createTable);

        // Insert sample libraries
        $sampleLibraries = [
            ['SIMATS Central Library', 'Saveetha University, Chennai', '8 AM - 8 PM', 13.0513, 80.0217],
            ['Anna Centenary Library', 'Kotturpuram, Chennai', '8 AM - 9 PM', 13.0107, 80.2417],
            ['Connemara Public Library', 'Egmore, Chennai', '9 AM - 7 PM', 13.0694, 80.2611],
            ['Madras Literary Society', 'College Road, Chennai', '10 AM - 6 PM', 13.0674, 80.2541],
            ['IIT Madras Library', 'IIT Campus, Chennai', '8 AM - 10 PM', 12.9916, 80.2336],
            ['British Council Library', 'Anna Salai, Chennai', '10 AM - 7 PM', 13.0606, 80.2495],
            ['American Library', 'Gemini Circle, Chennai', '10 AM - 6 PM', 13.0560, 80.2490]
        ];

        foreach ($sampleLibraries as $lib) {
            $stmt = $conn->prepare("INSERT INTO libraries (name, location, opening_hours, latitude, longitude) VALUES (?, ?, ?, ?, ?)");
            $stmt->bind_param("sssdd", $lib[0], $lib[1], $lib[2], $lib[3], $lib[4]);
            $stmt->execute();
            $stmt->close();
        }
    }

    // Get all libraries
    $sql = "SELECT l.*, 
            (SELECT COUNT(*) FROM books) as total_books
            FROM libraries l";

    $result = $conn->query($sql);

    if (!$result) {
        respond(false, 'Query failed: ' . $conn->error);
    }

    $libraries = [];
    while ($row = $result->fetch_assoc()) {
        $distance = 0;
        if ($userLat != 0 && $userLng != 0 && $row['latitude'] != 0 && $row['longitude'] != 0) {
            $distance = calculateDistance($userLat, $userLng, $row['latitude'], $row['longitude']);
        }

        $libraries[] = [
            'id' => (int) $row['id'],
            'name' => $row['name'],
            'location' => $row['location'],
            'address' => $row['address'] ?? '',
            'phone' => $row['phone'] ?? '',
            'email' => $row['email'] ?? '',
            'opening_hours' => $row['opening_hours'] ?? '9 AM - 6 PM',
            'latitude' => (float) $row['latitude'],
            'longitude' => (float) $row['longitude'],
            'total_books' => (int) $row['total_books'],
            'distance' => round($distance, 1)
        ];
    }

    // Sort by distance
    usort($libraries, function ($a, $b) {
        return $a['distance'] <=> $b['distance'];
    });

    respond(true, 'Libraries fetched successfully', [
        'libraries' => $libraries,
        'count' => count($libraries)
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("Get Libraries Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}

/**
 * Calculate distance between two points using Haversine formula
 */
function calculateDistance($lat1, $lng1, $lat2, $lng2)
{
    $earthRadius = 6371; // km

    $lat1 = deg2rad($lat1);
    $lng1 = deg2rad($lng1);
    $lat2 = deg2rad($lat2);
    $lng2 = deg2rad($lng2);

    $latDiff = $lat2 - $lat1;
    $lngDiff = $lng2 - $lng1;

    $a = sin($latDiff / 2) * sin($latDiff / 2) +
        cos($lat1) * cos($lat2) *
        sin($lngDiff / 2) * sin($lngDiff / 2);

    $c = 2 * atan2(sqrt($a), sqrt(1 - $a));

    return $earthRadius * $c;
}
?>