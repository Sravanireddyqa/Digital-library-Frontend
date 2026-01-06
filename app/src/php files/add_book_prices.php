<?php
/**
 * Add Real Book Prices
 * Sets realistic book prices based on book titles
 */

require_once 'db.php';

setHeaders();

try {
    $conn = getConnection();

    // Check if price column exists, add if not
    $columnsResult = $conn->query("SHOW COLUMNS FROM books LIKE 'price'");
    if ($columnsResult->num_rows == 0) {
        $conn->query("ALTER TABLE books ADD COLUMN price DECIMAL(10,2) DEFAULT 499.00");
    }

    // Set specific prices for known books (real market prices)
    $bookPrices = [
        // Programming & Technology
        "Effective Java" => 4500,
        "Clean Code" => 3999,
        "The Pragmatic Programmer" => 3500,
        "Introduction to Algorithms" => 5999,
        "Design Patterns" => 4299,
        "Head First Java" => 2999,
        "Java Complete Reference" => 3499,
        "Python Crash Course" => 2799,
        "Learning Python" => 3299,
        "C Programming" => 1999,

        // Literature & Fiction
        "The God of Small Things" => 399,
        "The White Tiger" => 350,
        "A Suitable Boy" => 699,
        "Midnight's Children" => 599,
        "The Guide" => 299,
        "Train to Pakistan" => 275,
        "The Palace of Illusions" => 450,

        // Science & Education
        "A Brief History of Time" => 599,
        "Cosmos" => 699,
        "The Origin of Species" => 499,
        "Sapiens" => 699,
        "Thinking Fast and Slow" => 599,

        // Self Help & Business
        "Rich Dad Poor Dad" => 399,
        "The Alchemist" => 350,
        "Atomic Habits" => 499,
        "Think and Grow Rich" => 299,
        "The 7 Habits" => 450,
        "How to Win Friends" => 350,

        // Indian Authors
        "Wings of Fire" => 350,
        "Ignited Minds" => 275,
        "My Experiments with Truth" => 299,
        "India 2020" => 325,

        // Academic & Reference
        "Engineering Mathematics" => 899,
        "Physics" => 799,
        "Chemistry" => 749,
        "Biology" => 699,
        "Computer Science" => 849
    ];

    $totalUpdated = 0;

    // Update specific books by title
    foreach ($bookPrices as $title => $price) {
        $escapedTitle = $conn->real_escape_string($title);
        $sql = "UPDATE books SET price = $price WHERE LOWER(title) LIKE LOWER('%$escapedTitle%')";
        $conn->query($sql);
        $totalUpdated += $conn->affected_rows;
    }

    // Set default prices for remaining books based on category
    $categoryPrices = [
        "UPDATE books SET price = 549 WHERE (price IS NULL OR price = 0) AND LOWER(category) LIKE '%fiction%'",
        "UPDATE books SET price = 799 WHERE (price IS NULL OR price = 0) AND LOWER(category) LIKE '%science%'",
        "UPDATE books SET price = 999 WHERE (price IS NULL OR price = 0) AND LOWER(category) LIKE '%technology%'",
        "UPDATE books SET price = 649 WHERE (price IS NULL OR price = 0) AND LOWER(category) LIKE '%history%'",
        "UPDATE books SET price = 1299 WHERE (price IS NULL OR price = 0) AND LOWER(category) LIKE '%medical%'",
        "UPDATE books SET price = 699 WHERE (price IS NULL OR price = 0) AND LOWER(category) LIKE '%education%'",
        "UPDATE books SET price = 449 WHERE (price IS NULL OR price = 0) AND LOWER(category) LIKE '%novel%'",
        "UPDATE books SET price = 899 WHERE (price IS NULL OR price = 0) AND LOWER(category) LIKE '%programming%'",
        "UPDATE books SET price = 999 WHERE (price IS NULL OR price = 0) AND LOWER(category) LIKE '%engineering%'",
        "UPDATE books SET price = 499 WHERE (price IS NULL OR price = 0)" // Default
    ];

    foreach ($categoryPrices as $sql) {
        $conn->query($sql);
        $totalUpdated += $conn->affected_rows;
    }

    // Get all books with their prices
    $result = $conn->query("SELECT id, title, price FROM books ORDER BY id");
    $books = [];
    while ($row = $result->fetch_assoc()) {
        $books[] = [
            'id' => $row['id'],
            'title' => $row['title'],
            'price' => '₹' . number_format($row['price'])
        ];
    }

    respond(true, "Updated prices for $totalUpdated books with real market prices", [
        'updated_count' => $totalUpdated,
        'books' => $books
    ]);

    $conn->close();

} catch (Exception $e) {
    respond(false, 'Error: ' . $e->getMessage());
}
?>