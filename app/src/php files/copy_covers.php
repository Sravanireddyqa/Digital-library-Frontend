<?php
/**
 * Copy Book Covers Script
 * Run this once to copy book covers to uploads folder
 */

// Source folder (Gemini artifacts)
$sourceFolder = 'C:/Users/srava/.gemini/antigravity/brain/180c680f-c7ab-4cc3-86b7-c47059d2317c/';

// Destination folder
$destFolder = __DIR__ . '/uploads/';

// Ensure uploads folder exists
if (!file_exists($destFolder)) {
    mkdir($destFolder, 0777, true);
}

$files = [
    'book_cover_mystic_quest' => 'mystic_quest.png',
    'book_cover_code_master' => 'code_master.png',
    'book_cover_silent_shadows' => 'silent_shadows.png',
    'book_cover_healthy_living' => 'healthy_living.png',
    'book_cover_space_odyssey' => 'space_odyssey.png'
];

echo "<h2>📚 Copying Book Covers</h2>";
echo "<hr>";

$copied = 0;
$failed = 0;

// Find and copy files
foreach ($files as $prefix => $newName) {
    // Find file with this prefix
    $pattern = $sourceFolder . $prefix . '*.png';
    $matches = glob($pattern);
    
    if (!empty($matches)) {
        $sourceFile = $matches[0];
        $destFile = $destFolder . $newName;
        
        if (copy($sourceFile, $destFile)) {
            echo "✅ Copied: <b>$newName</b><br>";
            $copied++;
        } else {
            echo "❌ Failed to copy: $newName<br>";
            $failed++;
        }
    } else {
        echo "⚠️ File not found: $prefix*.png<br>";
        $failed++;
    }
}

echo "<hr>";
echo "<p><b>Copied: $copied</b> | Failed: $failed</p>";

if ($copied > 0) {
    echo "<h3>🎉 Now run this SQL in phpMyAdmin:</h3>";
    echo "<textarea style='width:100%;height:300px;font-family:monospace;'>";
    echo "-- Insert 5 dummy books with cover URLs\n";
    echo "INSERT INTO books (title, author, category, isbn, price, stock, rating, description, cover_url, is_new) VALUES\n";
    echo "('The Mystic Quest', 'Alexandra Rivers', 'Fantasy', '9781234567890', 299, 10, 4.5, 'An epic fantasy adventure through enchanted forests and ancient magic.', 'http://10.36.207.135/digitallibrary_API/uploads/mystic_quest.png', 1),\n";
    echo "('Code Master: Python for Beginners', 'Dev Johnson', 'Technology', '9782345678901', 499, 15, 4.8, 'Learn Python programming from scratch with hands-on examples.', 'http://10.36.207.135/digitallibrary_API/uploads/code_master.png', 1),\n";
    echo "('Silent Shadows', 'Rachel Black', 'Thriller', '9783456789012', 249, 8, 4.3, 'A gripping crime thriller set in the dark streets of the city.', 'http://10.36.207.135/digitallibrary_API/uploads/silent_shadows.png', 1),\n";
    echo "('Healthy Living: 30 Day Guide', 'Dr. Sarah Green', 'Health & Wellness', '9784567890123', 199, 20, 4.6, 'Transform your lifestyle with this comprehensive 30-day wellness program.', 'http://10.36.207.135/digitallibrary_API/uploads/healthy_living.png', 1),\n";
    echo "('Space Odyssey 2150', 'Marcus Chen', 'Science Fiction', '9785678901234', 349, 12, 4.7, 'Humanitys greatest adventure beyond the stars in the year 2150.', 'http://10.36.207.135/digitallibrary_API/uploads/space_odyssey.png', 1);";
    echo "</textarea>";
}
?>
