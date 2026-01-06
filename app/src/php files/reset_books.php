<?php
/**
 * Reset Books Database with 500+ Books with Real Cover Images
 * RUN: http://localhost/digitallibrary_API/reset_books.php
 */

require_once 'db.php';

setHeaders();

try {
    $conn = getConnection();

    $conn->query("SET FOREIGN_KEY_CHECKS = 0");
    $conn->query("TRUNCATE TABLE wishlist");
    $conn->query("TRUNCATE TABLE reservations");
    $conn->query("TRUNCATE TABLE books");
    $conn->query("SET FOREIGN_KEY_CHECKS = 1");

    // Real books with actual ISBNs for cover images
    // Format: [title, author, isbn, category, pages, rating, price]
    $realBooks = [
        // Indian Literature
        ['The God of Small Things', 'Arundhati Roy', '9780679457312', 'Fiction', 340, 4.8, 299],
        ['Midnight\'s Children', 'Salman Rushdie', '9780099578512', 'Fiction', 647, 4.6, 350],
        ['The White Tiger', 'Aravind Adiga', '9781416562603', 'Fiction', 304, 4.4, 275],
        ['A Suitable Boy', 'Vikram Seth', '9780060786526', 'Fiction', 1349, 4.5, 450],
        ['The Inheritance of Loss', 'Kiran Desai', '9780802142818', 'Fiction', 324, 4.3, 320],
        ['Train to Pakistan', 'Khushwant Singh', '9780143065883', 'Fiction', 181, 4.5, 199],
        ['The Guide', 'R.K. Narayan', '9780143039648', 'Fiction', 220, 4.4, 175],
        ['Shantaram', 'Gregory David Roberts', '9780312330521', 'Fiction', 936, 4.5, 399],
        ['The Palace of Illusions', 'Chitra Banerjee Divakaruni', '9780385515993', 'Mythology', 360, 4.6, 299],
        ['The Immortals of Meluha', 'Amish Tripathi', '9789380658742', 'Mythology', 400, 4.3, 299],
        ['The Secret of the Nagas', 'Amish Tripathi', '9789381626344', 'Mythology', 400, 4.2, 299],
        ['Five Point Someone', 'Chetan Bhagat', '9788129135476', 'Fiction', 270, 4.0, 175],
        ['2 States', 'Chetan Bhagat', '9788129115300', 'Fiction', 280, 4.1, 175],
        ['Wings of Fire', 'APJ Abdul Kalam', '9788173711466', 'Biography', 180, 4.7, 225],

        // Classics
        ['To Kill a Mockingbird', 'Harper Lee', '9780061120084', 'Classic', 336, 4.9, 199],
        ['1984', 'George Orwell', '9780451524935', 'Classic', 328, 4.8, 180],
        ['The Great Gatsby', 'F. Scott Fitzgerald', '9780743273565', 'Classic', 180, 4.7, 150],
        ['Pride and Prejudice', 'Jane Austen', '9780141439518', 'Classic', 432, 4.8, 175],
        ['Jane Eyre', 'Charlotte Bronte', '9780141441146', 'Classic', 532, 4.6, 185],
        ['Wuthering Heights', 'Emily Bronte', '9780141439556', 'Classic', 416, 4.5, 170],
        ['Crime and Punishment', 'Fyodor Dostoevsky', '9780143107637', 'Classic', 671, 4.7, 250],
        ['The Brothers Karamazov', 'Fyodor Dostoevsky', '9780374528379', 'Classic', 796, 4.8, 299],
        ['War and Peace', 'Leo Tolstoy', '9781400079988', 'Classic', 1296, 4.6, 399],
        ['Anna Karenina', 'Leo Tolstoy', '9780143035008', 'Classic', 964, 4.7, 350],
        ['The Count of Monte Cristo', 'Alexandre Dumas', '9780140449266', 'Classic', 1276, 4.8, 325],
        ['Les Miserables', 'Victor Hugo', '9780451419439', 'Classic', 1488, 4.7, 375],
        ['The Picture of Dorian Gray', 'Oscar Wilde', '9780141439570', 'Classic', 254, 4.5, 165],
        ['Dracula', 'Bram Stoker', '9780141439846', 'Classic', 418, 4.4, 185],
        ['Frankenstein', 'Mary Shelley', '9780141439471', 'Classic', 280, 4.3, 160],
        ['Great Expectations', 'Charles Dickens', '9780141439563', 'Classic', 544, 4.4, 199],
        ['Oliver Twist', 'Charles Dickens', '9780141439747', 'Classic', 608, 4.3, 199],
        ['A Tale of Two Cities', 'Charles Dickens', '9780141439600', 'Classic', 489, 4.5, 185],
        ['The Old Man and the Sea', 'Ernest Hemingway', '9780684801223', 'Classic', 127, 4.5, 140],
        ['Of Mice and Men', 'John Steinbeck', '9780140186420', 'Classic', 107, 4.4, 130],
        ['The Grapes of Wrath', 'John Steinbeck', '9780143039433', 'Classic', 464, 4.5, 225],
        ['Brave New World', 'Aldous Huxley', '9780060850524', 'Classic', 288, 4.5, 175],
        ['The Catcher in the Rye', 'J.D. Salinger', '9780316769488', 'Classic', 277, 4.3, 165],
        ['Lord of the Flies', 'William Golding', '9780399501487', 'Classic', 224, 4.2, 150],
        ['Animal Farm', 'George Orwell', '9780451526342', 'Classic', 112, 4.6, 130],

        // Technology
        ['Clean Code', 'Robert C. Martin', '9780132350884', 'Technology', 464, 4.7, 599],
        ['Introduction to Algorithms', 'Thomas H. Cormen', '9780262033848', 'Technology', 1312, 4.8, 750],
        ['Design Patterns', 'Gang of Four', '9780201633610', 'Technology', 416, 4.6, 550],
        ['The Pragmatic Programmer', 'David Thomas', '9780135957059', 'Technology', 352, 4.8, 499],
        ['Code Complete', 'Steve McConnell', '9780735619678', 'Technology', 960, 4.7, 649],
        ['Cracking the Coding Interview', 'Gayle McDowell', '9780984782857', 'Technology', 687, 4.6, 549],
        ['Head First Java', 'Kathy Sierra', '9780596009205', 'Technology', 688, 4.5, 450],
        ['Effective Java', 'Joshua Bloch', '9780134685991', 'Technology', 416, 4.8, 520],
        ['Python Crash Course', 'Eric Matthes', '9781593279288', 'Technology', 544, 4.7, 399],
        ['JavaScript: The Good Parts', 'Douglas Crockford', '9780596517748', 'Technology', 176, 4.4, 350],
        ['Learning Python', 'Mark Lutz', '9781449355739', 'Technology', 1648, 4.3, 599],
        ['The C Programming Language', 'Brian Kernighan', '9780131103627', 'Technology', 272, 4.7, 399],
        ['Computer Networks', 'Andrew Tanenbaum', '9780132126953', 'Technology', 960, 4.5, 650],
        ['Artificial Intelligence', 'Stuart Russell', '9780136042594', 'Technology', 1152, 4.7, 799],
        ['Deep Learning', 'Ian Goodfellow', '9780262035613', 'Technology', 800, 4.6, 699],
        ['Grokking Algorithms', 'Aditya Bhargava', '9781617292231', 'Technology', 256, 4.7, 399],
        ['Refactoring', 'Martin Fowler', '9780134757599', 'Technology', 448, 4.6, 499],
        ['Clean Architecture', 'Robert C. Martin', '9780134494166', 'Technology', 432, 4.5, 499],
        ['Designing Data-Intensive Apps', 'Martin Kleppmann', '9781449373320', 'Technology', 616, 4.8, 599],

        // Non-Fiction
        ['Sapiens', 'Yuval Noah Harari', '9780062316097', 'Non-Fiction', 464, 4.7, 399],
        ['Homo Deus', 'Yuval Noah Harari', '9780062464316', 'Non-Fiction', 464, 4.5, 399],
        ['Thinking, Fast and Slow', 'Daniel Kahneman', '9780374533557', 'Non-Fiction', 499, 4.6, 375],
        ['Educated', 'Tara Westover', '9780399590504', 'Memoir', 352, 4.7, 350],
        ['Becoming', 'Michelle Obama', '9781524763138', 'Memoir', 448, 4.8, 425],
        ['A Brief History of Time', 'Stephen Hawking', '9780553380163', 'Science', 212, 4.6, 275],
        ['The Selfish Gene', 'Richard Dawkins', '9780199291151', 'Science', 360, 4.5, 299],
        ['Cosmos', 'Carl Sagan', '9780345539434', 'Science', 396, 4.7, 350],
        ['Guns, Germs, and Steel', 'Jared Diamond', '9780393354324', 'History', 528, 4.5, 375],
        ['The Art of War', 'Sun Tzu', '9781599869773', 'Philosophy', 273, 4.5, 199],
        ['Meditations', 'Marcus Aurelius', '9780140449334', 'Philosophy', 254, 4.6, 199],
        ['Man\'s Search for Meaning', 'Viktor Frankl', '9780807014295', 'Psychology', 184, 4.7, 225],
        ['Outliers', 'Malcolm Gladwell', '9780316017930', 'Non-Fiction', 309, 4.5, 325],
        ['Blink', 'Malcolm Gladwell', '9780316010665', 'Non-Fiction', 296, 4.3, 299],
        ['The Tipping Point', 'Malcolm Gladwell', '9780316346627', 'Non-Fiction', 301, 4.4, 299],

        // Self-Help
        ['Atomic Habits', 'James Clear', '9780735211292', 'Self-Help', 320, 4.8, 350],
        ['The Power of Habit', 'Charles Duhigg', '9780812981605', 'Self-Help', 400, 4.5, 299],
        ['Deep Work', 'Cal Newport', '9781455586691', 'Self-Help', 304, 4.6, 325],
        ['The 7 Habits of Highly Effective People', 'Stephen Covey', '9781982137274', 'Self-Help', 432, 4.6, 350],
        ['How to Win Friends', 'Dale Carnegie', '9780671027032', 'Self-Help', 288, 4.7, 275],
        ['Think and Grow Rich', 'Napoleon Hill', '9781585424337', 'Self-Help', 238, 4.5, 225],
        ['The Subtle Art of Not Giving a F*ck', 'Mark Manson', '9780062457714', 'Self-Help', 224, 4.4, 299],
        ['Can\'t Hurt Me', 'David Goggins', '9781544512280', 'Self-Help', 364, 4.7, 375],
        ['12 Rules for Life', 'Jordan Peterson', '9780345816023', 'Self-Help', 409, 4.5, 399],
        ['The Four Agreements', 'Don Miguel Ruiz', '9781878424310', 'Self-Help', 160, 4.6, 225],
        ['The Alchemist', 'Paulo Coelho', '9780062315007', 'Fiction', 208, 4.6, 225],
        ['Rich Dad Poor Dad', 'Robert Kiyosaki', '9781612680194', 'Business', 336, 4.5, 299],
        ['The Psychology of Money', 'Morgan Housel', '9780857197689', 'Business', 256, 4.7, 325],

        // Thriller/Mystery
        ['Gone Girl', 'Gillian Flynn', '9780307588371', 'Thriller', 415, 4.4, 299],
        ['The Girl on the Train', 'Paula Hawkins', '9781594634024', 'Thriller', 323, 4.3, 275],
        ['The Da Vinci Code', 'Dan Brown', '9780307474278', 'Thriller', 489, 4.4, 299],
        ['Angels and Demons', 'Dan Brown', '9781416524793', 'Thriller', 736, 4.3, 275],
        ['The Silent Patient', 'Alex Michaelides', '9781250301697', 'Thriller', 336, 4.5, 325],
        ['And Then There Were None', 'Agatha Christie', '9780062073488', 'Mystery', 272, 4.7, 199],
        ['Murder on the Orient Express', 'Agatha Christie', '9780062693662', 'Mystery', 256, 4.6, 199],
        ['The Girl with the Dragon Tattoo', 'Stieg Larsson', '9780307454546', 'Thriller', 672, 4.5, 375],

        // Fantasy/Sci-Fi
        ['Harry Potter and the Sorcerer\'s Stone', 'J.K. Rowling', '9780590353427', 'Fantasy', 309, 4.9, 350],
        ['Harry Potter and the Chamber of Secrets', 'J.K. Rowling', '9780439064873', 'Fantasy', 341, 4.8, 350],
        ['Harry Potter and the Prisoner of Azkaban', 'J.K. Rowling', '9780439136365', 'Fantasy', 435, 4.9, 350],
        ['Harry Potter and the Goblet of Fire', 'J.K. Rowling', '9780439139595', 'Fantasy', 734, 4.8, 399],
        ['Harry Potter and the Deathly Hallows', 'J.K. Rowling', '9780545139700', 'Fantasy', 759, 4.8, 425],
        ['The Hobbit', 'J.R.R. Tolkien', '9780547928227', 'Fantasy', 300, 4.8, 299],
        ['The Fellowship of the Ring', 'J.R.R. Tolkien', '9780544003415', 'Fantasy', 423, 4.9, 350],
        ['A Game of Thrones', 'George R.R. Martin', '9780553573404', 'Fantasy', 694, 4.7, 399],
        ['The Name of the Wind', 'Patrick Rothfuss', '9780756404741', 'Fantasy', 662, 4.7, 375],
        ['Mistborn: The Final Empire', 'Brandon Sanderson', '9780765360960', 'Fantasy', 541, 4.7, 350],
        ['Dune', 'Frank Herbert', '9780441172719', 'Sci-Fi', 688, 4.7, 350],
        ['The Hunger Games', 'Suzanne Collins', '9780439023481', 'Sci-Fi', 374, 4.6, 275],
        ['Catching Fire', 'Suzanne Collins', '9780439023498', 'Sci-Fi', 391, 4.5, 275],
        ['Ender\'s Game', 'Orson Scott Card', '9780812550702', 'Sci-Fi', 324, 4.6, 250],
        ['Fahrenheit 451', 'Ray Bradbury', '9781451673319', 'Sci-Fi', 249, 4.5, 225],
        ['The Martian', 'Andy Weir', '9780553418026', 'Sci-Fi', 369, 4.7, 325],
        ['Ready Player One', 'Ernest Cline', '9780307887443', 'Sci-Fi', 374, 4.5, 325],
        ['Foundation', 'Isaac Asimov', '9780553293357', 'Sci-Fi', 244, 4.5, 250],

        // Romance
        ['The Notebook', 'Nicholas Sparks', '9781455582877', 'Romance', 214, 4.4, 225],
        ['A Walk to Remember', 'Nicholas Sparks', '9781455549702', 'Romance', 240, 4.5, 225],
        ['Me Before You', 'Jojo Moyes', '9780143124542', 'Romance', 369, 4.5, 299],
        ['The Fault in Our Stars', 'John Green', '9780142424179', 'Romance', 313, 4.5, 275],
        ['Outlander', 'Diana Gabaldon', '9780440212560', 'Romance', 850, 4.5, 375],
        ['It Ends with Us', 'Colleen Hoover', '9781501110368', 'Romance', 376, 4.6, 299],
        ['Verity', 'Colleen Hoover', '9781538724736', 'Romance', 314, 4.4, 299],

        // Business
        ['The Lean Startup', 'Eric Ries', '9780307887894', 'Business', 336, 4.6, 350],
        ['Zero to One', 'Peter Thiel', '9780804139298', 'Business', 224, 4.5, 299],
        ['Good to Great', 'Jim Collins', '9780066620992', 'Business', 320, 4.4, 325],
        ['Start with Why', 'Simon Sinek', '9781591846444', 'Business', 256, 4.5, 299],
        ['Shoe Dog', 'Phil Knight', '9781501135927', 'Business', 386, 4.7, 375],
        ['Steve Jobs', 'Walter Isaacson', '9781451648539', 'Biography', 656, 4.6, 425],
        ['Elon Musk', 'Ashlee Vance', '9780062301239', 'Biography', 400, 4.6, 375],
        ['The 4-Hour Workweek', 'Tim Ferriss', '9780307465351', 'Business', 416, 4.4, 350],
        ['Never Split the Difference', 'Chris Voss', '9780062407801', 'Business', 288, 4.6, 350],

        // Children
        ['Charlotte\'s Web', 'E.B. White', '9780064400558', 'Children', 184, 4.7, 175],
        ['Matilda', 'Roald Dahl', '9780142410370', 'Children', 240, 4.6, 199],
        ['Charlie and the Chocolate Factory', 'Roald Dahl', '9780142410318', 'Children', 176, 4.5, 175],
        ['The BFG', 'Roald Dahl', '9780142410387', 'Children', 208, 4.5, 175],
        ['Percy Jackson: Lightning Thief', 'Rick Riordan', '9780786838653', 'Fantasy', 377, 4.6, 275],
        ['Divergent', 'Veronica Roth', '9780062024039', 'Sci-Fi', 487, 4.4, 299],
        ['The Maze Runner', 'James Dashner', '9780385737951', 'Sci-Fi', 375, 4.4, 275],
        ['The Giver', 'Lois Lowry', '9780544336261', 'Sci-Fi', 208, 4.5, 225],
        ['Wonder', 'R.J. Palacio', '9780375869020', 'Children', 315, 4.7, 275],
    ];

    // Insert real books
    $stmt = $conn->prepare("INSERT INTO books (title, author, isbn, category, publisher, published_date, pages, description, cover_url, rating, price, stock) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

    $count = 0;
    $categories = [];

    foreach ($realBooks as $b) {
        $title = $b[0];
        $author = $b[1];
        $isbn = $b[2];
        $category = $b[3];
        $pages = $b[4];
        $rating = $b[5];
        $price = $b[6];
        $publisher = 'Publisher';
        $publishedDate = '2020-01-01';
        $description = "A captivating $category book by $author.";
        $coverUrl = 'https://covers.openlibrary.org/b/isbn/' . $isbn . '-L.jpg';
        $stock = rand(3, 15);

        $stmt->bind_param("ssssssisdddi", $title, $author, $isbn, $category, $publisher, $publishedDate, $pages, $description, $coverUrl, $rating, $price, $stock);
        if ($stmt->execute()) {
            $count++;
            if (!isset($categories[$category]))
                $categories[$category] = 0;
            $categories[$category]++;
        }
    }

    // Generate additional books to reach 500
    $moreBooks = [
        ['The Road', 'Cormac McCarthy', '9780307387899', 'Fiction'],
        ['Life of Pi', 'Yann Martel', '9780156027328', 'Fiction'],
        ['The Kite Runner', 'Khaled Hosseini', '9781594631931', 'Fiction'],
        ['A Thousand Splendid Suns', 'Khaled Hosseini', '9781594483851', 'Fiction'],
        ['The Book Thief', 'Markus Zusak', '9780375842207', 'Fiction'],
        ['All the Light We Cannot See', 'Anthony Doerr', '9781501173219', 'Fiction'],
        ['Where the Crawdads Sing', 'Delia Owens', '9780735219106', 'Fiction'],
        ['Normal People', 'Sally Rooney', '9781984822185', 'Fiction'],
        ['Little Women', 'Louisa May Alcott', '9780147514011', 'Classic'],
        ['The Secret History', 'Donna Tartt', '9781400031702', 'Fiction'],
        ['The Goldfinch', 'Donna Tartt', '9780316055437', 'Fiction'],
        ['Norwegian Wood', 'Haruki Murakami', '9780375704024', 'Fiction'],
        ['Kafka on the Shore', 'Haruki Murakami', '9781400079278', 'Fiction'],
        ['1Q84', 'Haruki Murakami', '9780307593313', 'Fiction'],
        ['The Wind-Up Bird Chronicle', 'Haruki Murakami', '9780679775430', 'Fiction'],
    ];

    foreach ($moreBooks as $b) {
        $title = $b[0];
        $author = $b[1];
        $isbn = $b[2];
        $category = $b[3];
        $pages = rand(200, 500);
        $rating = round(rand(40, 48) / 10, 1);
        $price = rand(200, 400);
        $publisher = 'Publisher';
        $publishedDate = '2020-01-01';
        $description = "A captivating $category book by $author.";
        $coverUrl = 'https://covers.openlibrary.org/b/isbn/' . $isbn . '-L.jpg';
        $stock = rand(3, 15);

        $stmt->bind_param("ssssssisdddi", $title, $author, $isbn, $category, $publisher, $publishedDate, $pages, $description, $coverUrl, $rating, $price, $stock);
        if ($stmt->execute()) {
            $count++;
            if (!isset($categories[$category]))
                $categories[$category] = 0;
            $categories[$category]++;
        }
    }

    // Generate more books with placeholder images
    $additionalAuthors = ['John Smith', 'Jane Doe', 'Michael Brown', 'Emily Davis', 'Robert Wilson', 'Sarah Miller'];
    $additionalCategories = ['Fiction', 'Classic', 'Technology', 'Non-Fiction', 'Self-Help', 'Business', 'Thriller', 'Fantasy', 'Sci-Fi', 'Romance', 'Mystery', 'Biography', 'History', 'Science', 'Philosophy'];
    $titlePrefixes = ['The Art of', 'Mastering', 'Understanding', 'Guide to', 'Secrets of', 'Introduction to', 'Advanced', 'Essential', 'Power of', 'Discovering'];
    $titleTopics = ['Life', 'Success', 'Mind', 'Universe', 'Time', 'Love', 'Power', 'Knowledge', 'Dreams', 'Adventure', 'Wisdom', 'Nature', 'Future', 'Science', 'Art'];

    $remaining = 500 - $count;
    for ($i = 0; $i < $remaining; $i++) {
        $title = $titlePrefixes[array_rand($titlePrefixes)] . ' ' . $titleTopics[array_rand($titleTopics)];
        $author = $additionalAuthors[array_rand($additionalAuthors)];
        $category = $additionalCategories[array_rand($additionalCategories)];
        $pages = rand(150, 600);
        $rating = round(rand(35, 48) / 10, 1);
        $price = rand(150, 500);
        $isbn = '978' . str_pad($count + $i + 1000, 10, '0', STR_PAD_LEFT);
        $publisher = 'Publisher House';
        $publishedDate = '2020-01-01';
        $topic = $titleTopics[array_rand($titleTopics)];
        $description = "An engaging $category book about $topic by $author.";
        // Use placeholder for generated books
        $coverUrl = 'https://via.placeholder.com/200x300/7C3AED/FFFFFF?text=' . urlencode(substr($title, 0, 15));
        $stock = rand(3, 20);

        $stmt->bind_param("ssssssisdddi", $title, $author, $isbn, $category, $publisher, $publishedDate, $pages, $description, $coverUrl, $rating, $price, $stock);
        if ($stmt->execute()) {
            $count++;
            if (!isset($categories[$category]))
                $categories[$category] = 0;
            $categories[$category]++;
        }
    }

    $stmt->close();
    $conn->close();

    echo "<h1>✅ Database Reset Complete!</h1>";
    echo "<p>Added <strong>$count</strong> books with cover images.</p>";
    echo "<h2>Categories:</h2><ul>";
    arsort($categories);
    foreach ($categories as $cat => $catCount) {
        echo "<li><strong>$cat</strong>: $catCount books</li>";
    }
    echo "</ul>";
    echo "<p><a href='get_books.php'>View API</a></p>";

} catch (Exception $e) {
    echo "<h1>❌ Error</h1><p>" . $e->getMessage() . "</p>";
}
?>