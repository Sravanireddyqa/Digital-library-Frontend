<?php
/**
 * Populate Books Script
 * Adds sample books across 10 categories for testing
 */

require_once 'db.php';

setHeaders();

try {
    $conn = getConnection();

    // Optional: Clear existing books (comment out if you want to keep existing books)
    // $conn->query("TRUNCATE TABLE books");

    $booksAdded = 0;
    $errors = [];

    // Sample books data across 10 categories
    $books = [
        // FICTION (8 books)
        [
            'title' => 'To Kill a Mockingbird',
            'author' => 'Harper Lee',
            'category' => 'Fiction',
            'description' => 'A gripping tale of racial injustice and childhood innocence in the American South.',
            'price' => 399.00,
            'stock' => 12,
            'rating' => 4.8,
            'isbn' => 'ISBN-978-0061120084',
            'publisher' => 'HarperCollins',
            'published_date' => '1960',
            'pages' => 324
        ],
        [
            'title' => 'The Great Gatsby',
            'author' => 'F. Scott Fitzgerald',
            'category' => 'Fiction',
            'description' => 'A masterpiece about the American Dream, wealth, and lost love in the Jazz Age.',
            'price' => 299.00,
            'stock' => 10,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0743273565',
            'publisher' => 'Scribner',
            'published_date' => '1925',
            'pages' => 180
        ],
        [
            'title' => 'One Hundred Years of Solitude',
            'author' => 'Gabriel García Márquez',
            'category' => 'Fiction',
            'description' => 'The multi-generational story of the Buendía family in the fictional town of Macondo.',
            'price' => 450.00,
            'stock' => 8,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-0060883287',
            'publisher' => 'Harper & Row',
            'published_date' => '1967',
            'pages' => 417
        ],
        [
            'title' => 'Pride and Prejudice',
            'author' => 'Jane Austen',
            'category' => 'Fiction',
            'description' => 'A romantic novel of manners that critiques the British landed gentry at the end of the 18th century.',
            'price' => 349.00,
            'stock' => 15,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0141439518',
            'publisher' => 'Penguin Classics',
            'published_date' => '1813',
            'pages' => 432
        ],
        [
            'title' => 'The God of Small Things',
            'author' => 'Arundhati Roy',
            'category' => 'Fiction',
            'description' => 'A story about the childhood experiences of fraternal twins whose lives are destroyed by social conventions.',
            'price' => 399.00,
            'stock' => 10,
            'rating' => 4.5,
            'isbn' => 'ISBN-978-0006550686',
            'publisher' => 'IndiaInk',
            'published_date' => '1997',
            'pages' => 340
        ],
        [
            'title' => 'The Kite Runner',
            'author' => 'Khaled Hosseini',
            'category' => 'Fiction',
            'description' => 'A powerful story of friendship, betrayal, and redemption set in Afghanistan.',
            'price' => 375.00,
            'stock' => 11,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-1594480003',
            'publisher' => 'Riverhead Books',
            'published_date' => '2003',
            'pages' => 371
        ],
        [
            'title' => 'Life of Pi',
            'author' => 'Yann Martel',
            'category' => 'Fiction',
            'description' => 'A fantasy adventure novel about an Indian boy who survives a shipwreck and is stranded in the Pacific Ocean.',
            'price' => 425.00,
            'stock' => 9,
            'rating' => 4.4,
            'isbn' => 'ISBN-978-0156027328',
            'publisher' => 'Mariner Books',
            'published_date' => '2001',
            'pages' => 319
        ],
        [
            'title' => 'Midnight\'s Children',
            'author' => 'Salman Rushdie',
            'category' => 'Fiction',
            'description' => 'A novel about children born at midnight on India\'s independence, with magical realist storytelling.',
            'price' => 499.00,
            'stock' => 7,
            'rating' => 4.3,
            'isbn' => 'ISBN-978-0099578512',
            'publisher' => 'Vintage',
            'published_date' => '1981',
            'pages' => 647
        ],

        // HORROR (7 books)
        [
            'title' => 'The Shining',
            'author' => 'Stephen King',
            'category' => 'Horror',
            'description' => 'A family heads to an isolated hotel for the winter where a sinister presence influences the father.',
            'price' => 450.00,
            'stock' => 10,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0307743657',
            'publisher' => 'Doubleday',
            'published_date' => '1977',
            'pages' => 447
        ],
        [
            'title' => 'IT',
            'author' => 'Stephen King',
            'category' => 'Horror',
            'description' => 'A group of friends face their worst nightmares when they square off against an evil clown named Pennywise.',
            'price' => 599.00,
            'stock' => 8,
            'rating' => 4.8,
            'isbn' => 'ISBN-978-1501142970',
            'publisher' => 'Viking',
            'published_date' => '1986',
            'pages' => 1138
        ],
        [
            'title' => 'Dracula',
            'author' => 'Bram Stoker',
            'category' => 'Horror',
            'description' => 'The classic vampire tale that introduced Count Dracula to the world.',
            'price' => 299.00,
            'stock' => 15,
            'rating' => 4.5,
            'isbn' => 'ISBN-978-0141439846',
            'publisher' => 'Penguin Classics',
            'published_date' => '1897',
            'pages' => 418
        ],
        [
            'title' => 'Frankenstein',
            'author' => 'Mary Shelley',
            'category' => 'Horror',
            'description' => 'The story of Victor Frankenstein and the monster he created, exploring themes of ambition and humanity.',
            'price' => 249.00,
            'stock' => 12,
            'rating' => 4.4,
            'isbn' => 'ISBN-978-0486282114',
            'publisher' => 'Dover Publications',
            'published_date' => '1818',
            'pages' => 280
        ],
        [
            'title' => 'The Exorcist',
            'author' => 'William Peter Blatty',
            'category' => 'Horror',
            'description' => 'A terrifying tale of demonic possession and the battle between good and evil.',
            'price' => 399.00,
            'stock' => 9,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-0061007224',
            'publisher' => 'Harper & Row',
            'published_date' => '1971',
            'pages' => 385
        ],
        [
            'title' => 'Pet Sematary',
            'author' => 'Stephen King',
            'category' => 'Horror',
            'description' => 'A family discovers a mysterious burial ground in the woods behind their home with horrifying consequences.',
            'price' => 425.00,
            'stock' => 10,
            'rating' => 4.5,
            'isbn' => 'ISBN-978-0743412285',
            'publisher' => 'Doubleday',
            'published_date' => '1983',
            'pages' => 374
        ],
        [
            'title' => 'The Haunting of Hill House',
            'author' => 'Shirley Jackson',
            'category' => 'Horror',
            'description' => 'Four seekers arrive at a notoriously unfriendly pile called Hill House, where they encounter true terror.',
            'price' => 349.00,
            'stock' => 11,
            'rating' => 4.3,
            'isbn' => 'ISBN-978-0143039983',
            'publisher' => 'Penguin Books',
            'published_date' => '1959',
            'pages' => 246
        ],

        // ROMANCE (7 books)
        [
            'title' => 'Me Before You',
            'author' => 'Jojo Moyes',
            'category' => 'Romance',
            'description' => 'A life-affirming love story between a quirky girl and a paralyzed man.',
            'price' => 375.00,
            'stock' => 14,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-0143124542',
            'publisher' => 'Penguin Books',
            'published_date' => '2012',
            'pages' => 369
        ],
        [
            'title' => 'The Notebook',
            'author' => 'Nicholas Sparks',
            'category' => 'Romance',
            'description' => 'An enduring love story that spans decades, proving true love never dies.',
            'price' => 325.00,
            'stock' => 12,
            'rating' => 4.5,
            'isbn' => 'ISBN-978-0446605236',
            'publisher' => 'Warner Books',
            'published_date' => '1996',
            'pages' => 214
        ],
        [
            'title' => 'Outlander',
            'author' => 'Diana Gabaldon',
            'category' => 'Romance',
            'description' => 'A WWII nurse is transported back to 18th-century Scotland where she meets a Highland warrior.',
            'price' => 499.00,
            'stock' => 8,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0385319959',
            'publisher' => 'Delacorte Press',
            'published_date' => '1991',
            'pages' => 627
        ],
        [
            'title' => 'The Time Traveler\'s Wife',
            'author' => 'Audrey Niffenegger',
            'category' => 'Romance',
            'description' => 'A unique love story about a man with a genetic disorder causing him to time travel unpredictably.',
            'price' => 425.00,
            'stock' => 10,
            'rating' => 4.4,
            'isbn' => 'ISBN-978-015602943',
            'publisher' => 'MacAdam/Cage',
            'published_date' => '2003',
            'pages' => 518
        ],
        [
            'title' => 'The Fault in Our Stars',
            'author' => 'John Green',
            'category' => 'Romance',
            'description' => 'Two cancer patients fall in love and embark on a life-changing journey together.',
            'price' => 349.00,
            'stock' => 15,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-0142424179',
            'publisher' => 'Dutton Books',
            'published_date' => '2012',
            'pages' => 313
        ],
        [
            'title' => 'Jane Eyre',
            'author' => 'Charlotte Brontë',
            'category' => 'Romance',
            'description' => 'The story of a young governess who falls in love with her mysterious employer, Mr. Rochester.',
            'price' => 299.00,
            'stock' => 13,
            'rating' => 4.5,
            'isbn' => 'ISBN-978-0141441146',
            'publisher' => 'Penguin Classics',
            'published_date' => '1847',
            'pages' => 507
        ],
        [
            'title' => 'Wuthering Heights',
            'author' => 'Emily Brontë',
            'category' => 'Romance',
            'description' => 'A passionate and tumultuous love story set on the Yorkshire moors.',
            'price' => 325.00,
            'stock' => 11,
            'rating' => 4.3,
            'isbn' => 'ISBN-978-0141439556',
            'publisher' => 'Penguin Classics',
            'published_date' => '1847',
            'pages' => 416
        ],

        // THRILLER (7 books)
        [
            'title' => 'The Girl with the Dragon Tattoo',
            'author' => 'Stieg Larsson',
            'category' => 'Thriller',
            'description' => 'A journalist and a hacker investigate a wealthy family\'s dark secrets.',
            'price' => 449.00,
            'stock' => 11,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0307454546',
            'publisher' => 'Vintage Crime',
            'published_date' => '2005',
            'pages' => 465
        ],
        [
            'title' => 'Gone Girl',
            'author' => 'Gillian Flynn',
            'category' => 'Thriller',
            'description' => 'A marriage\'s dark secrets unravel when a wife goes missing on her fifth anniversary.',
            'price' => 425.00,
            'stock' => 13,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-0307588371',
            'publisher' => 'Crown Publishing',
            'published_date' => '2012',
            'pages' => 415
        ],
        [
            'title' => 'The Da Vinci Code',
            'author' => 'Dan Brown',
            'category' => 'Thriller',
            'description' => 'A symbologist uncovers a conspiracy while investigating a murder at the Louvre.',
            'price' => 399.00,
            'stock' => 14,
            'rating' => 4.5,
            'isbn' => 'ISBN-978-0307474278',
            'publisher' => 'Doubleday',
            'published_date' => '2003',
            'pages' => 489
        ],
        [
            'title' => 'The Woman in the Window',
            'author' => 'A. J. Finn',
            'category' => 'Thriller',
            'description' => 'An agoraphobic psychologist witnesses something shocking in her neighbor\'s house.',
            'price' => 375.00,
            'stock' => 10,
            'rating' => 4.4,
            'isbn' => 'ISBN-978-0062678416',
            'publisher' => 'William Morrow',
            'published_date' => '2018',
            'pages' => 427
        ],
        [
            'title' => 'The Silent Patient',
            'author' => 'Alex Michaelides',
            'category' => 'Thriller',
            'description' => 'A woman shoots her husband and then never speaks again. A therapist is determined to uncover her motive.',
            'price' => 450.00,
            'stock' => 12,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-1250301697',
            'publisher' => 'Celadon Books',
            'published_date' => '2019',
            'pages' => 325
        ],
        [
            'title' => 'The Bourne Identity',
            'author' => 'Robert Ludlum',
            'category' => 'Thriller',
            'description' => 'An amnesiac man with extraordinary skills must discover his identity while being hunted.',
            'price' => 399.00,
            'stock' => 9,
            'rating' => 4.5,
            'isbn' => 'ISBN-978-0553593549',
            'publisher' => 'Bantam',
            'published_date' => '1980',
            'pages' => 523
        ],
        [
            'title' => 'Big Little Lies',
            'author' => 'Liane Moriarty',
            'category' => 'Thriller',
            'description' => 'Three women\'s seemingly perfect lives unravel to the point of murder.',
            'price' => 425.00,
            'stock' => 11,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-0399167065',
            'publisher' => 'G.P. Putnam\'s Sons',
            'published_date' => '2014',
            'pages' => 460
        ],

        // SCIENCE FICTION (7 books)
        [
            'title' => 'Dune',
            'author' => 'Frank Herbert',
            'category' => 'Science Fiction',
            'description' => 'A sweeping epic about politics, religion, and ecology on a desert planet.',
            'price' => 549.00,
            'stock' => 10,
            'rating' => 4.8,
            'isbn' => 'ISBN-978-0441172719',
            'publisher' => 'Ace',
            'published_date' => '1965',
            'pages' => 688
        ],
        [
            'title' => 'The Martian',
            'author' => 'Andy Weir',
            'category' => 'Science Fiction',
            'description' => 'An astronaut must survive on Mars using science and ingenuity after being left behind.',
            'price' => 425.00,
            'stock' => 12,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0553418026',
            'publisher' => 'Crown',
            'published_date' => '2011',
            'pages' => 369
        ],
        [
            'title' => 'Foundation',
            'author' => 'Isaac Asimov',
            'category' => 'Science Fiction',
            'description' => 'A mathematician develops psychohistory to predict and mitigate the fall of civilization.',
            'price' => 399.00,
            'stock' => 11,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-0553293357',
            'publisher' => 'Spectra',
            'published_date' => '1951',
            'pages' => 255
        ],
        [
            'title' => 'Ender\'s Game',
            'author' => 'Orson Scott Card',
            'category' => 'Science Fiction',
            'description' => 'A young boy is trained in military strategy to fight an alien invasion.',
            'price' => 375.00,
            'stock' => 13,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0812550702',
            'publisher' => 'Tor Books',
            'published_date' => '1985',
            'pages' => 324
        ],
        [
            'title' => 'Neuromancer',
            'author' => 'William Gibson',
            'category' => 'Science Fiction',
            'description' => 'A washed-up computer hacker is hired for one last job in cyberspace.',
            'price' => 449.00,
            'stock' => 8,
            'rating' => 4.4,
            'isbn' => 'ISBN-978-0441569595',
            'publisher' => 'Ace',
            'published_date' => '1984',
            'pages' => 271
        ],
        [
            'title' => 'The Hitchhiker\'s Guide to the Galaxy',
            'author' => 'Douglas Adams',
            'category' => 'Science Fiction',
            'description' => 'A comedic space adventure following the last human and his alien friend.',
            'price' => 325.00,
            'stock' => 15,
            'rating' => 4.8,
            'isbn' => 'ISBN-978-0345391803',
            'publisher' => 'Del Rey',
            'published_date' => '1979',
            'pages' => 224
        ],
        [
            'title' => '1984',
            'author' => 'George Orwell',
            'category' => 'Science Fiction',
            'description' => 'A dystopian novel about totalitarian surveillance and thought control.',
            'price' => 299.00,
            'stock' => 14,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0451524935',
            'publisher' => 'Signet Classic',
            'published_date' => '1949',
            'pages' => 328
        ],

        // FANTASY (7 books)
        [
            'title' => 'The Hobbit',
            'author' => 'J.R.R. Tolkien',
            'category' => 'Fantasy',
            'description' => 'Bilbo Baggins embarks on an unexpected adventure to reclaim treasure from a dragon.',
            'price' => 399.00,
            'stock' => 12,
            'rating' => 4.8,
            'isbn' => 'ISBN-978-0547928227',
            'publisher' => 'Mariner Books',
            'published_date' => '1937',
            'pages' => 310
        ],
        [
            'title' => 'Harry Potter and the Sorcerer\'s Stone',
            'author' => 'J.K. Rowling',
            'category' => 'Fantasy',
            'description' => 'An orphan boy discovers he is a wizard and attends Hogwarts School of Witchcraft.',
            'price' => 450.00,
            'stock' => 15,
            'rating' => 4.9,
            'isbn' => 'ISBN-978-0439708180',
            'publisher' => 'Scholastic',
            'published_date' => '1997',
            'pages' => 309
        ],
        [
            'title' => 'A Game of Thrones',
            'author' => 'George R.R. Martin',
            'category' => 'Fantasy',
            'description' => 'Noble families vie for control of the Iron Throne in the Seven Kingdoms of Westeros.',
            'price' => 549.00,
            'stock' => 10,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0553103540',
            'publisher' => 'Bantam Spectra',
            'published_date' => '1996',
            'pages' => 694
        ],
        [
            'title' => 'The Name of the Wind',
            'author' => 'Patrick Rothfuss',
            'category' => 'Fantasy',
            'description' => 'A gifted young man grows up to become the most notorious wizard his world has ever seen.',
            'price' => 499.00,
            'stock' => 9,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-0756404741',
            'publisher' => 'DAW Books',
            'published_date' => '2007',
            'pages' => 662
        ],
        [
            'title' => 'The Way of Kings',
            'author' => 'Brandon Sanderson',
            'category' => 'Fantasy',
            'description' => 'An epic fantasy about war, magic, and the struggle for honor in a world of storms.',
            'price' => 599.00,
            'stock' => 8,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0765326355',
            'publisher' => 'Tor Books',
            'published_date' => '2010',
            'pages' => 1007
        ],
        [
            'title' => 'The Chronicles of Narnia',
            'author' => 'C.S. Lewis',
            'category' => 'Fantasy',
            'description' => 'Children discover a magical land accessed through a wardrobe and must save it from evil.',
            'price' => 425.00,
            'stock' => 11,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-0066238500',
            'publisher' => 'HarperCollins',
            'published_date' => '1950',
            'pages' => 767
        ],
        [
            'title' => 'Mistborn: The Final Empire',
            'author' => 'Brandon Sanderson',
            'category' => 'Fantasy',
            'description' => 'A street urchin with magical abilities joins a rebellion to overthrow the immortal emperor.',
            'price' => 475.00,
            'stock' => 10,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0765350381',
            'publisher' => 'Tor Books',
            'published_date' => '2006',
            'pages' => 541
        ],

        // BIOGRAPHY (6 books)
        [
            'title' => 'Steve Jobs',
            'author' => 'Walter Isaacson',
            'category' => 'Biography',
            'description' => 'The authorized biography of the visionary co-founder of Apple.',
            'price' => 599.00,
            'stock' => 10,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-1451648539',
            'publisher' => 'Simon & Schuster',
            'published_date' => '2011',
            'pages' => 656
        ],
        [
            'title' => 'Becoming',
            'author' => 'Michelle Obama',
            'category' => 'Biography',
            'description' => 'The memoir of the former First Lady of the United States.',
            'price' => 549.00,
            'stock' => 12,
            'rating' => 4.8,
            'isbn' => 'ISBN-978-1524763138',
            'publisher' => 'Crown',
            'published_date' => '2018',
            'pages' => 448
        ],
        [
            'title' => 'The Diary of a Young Girl',
            'author' => 'Anne Frank',
            'category' => 'Biography',
            'description' => 'The powerful diary of a Jewish girl hiding from the Nazis during World War II.',
            'price' => 299.00,
            'stock' => 15,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0307594006',
            'publisher' => 'Bantam',
            'published_date' => '1947',
            'pages' => 283
        ],
        [
            'title' => 'Long Walk to Freedom',
            'author' => 'Nelson Mandela',
            'category' => 'Biography',
            'description' => 'The autobiography of South Africa\'s first Black president and anti-apartheid revolutionary.',
            'price' => 499.00,
            'stock' => 9,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0316548182',
            'publisher' => 'Little, Brown',
            'published_date' => '1994',
            'pages' => 656
        ],
        [
            'title' => 'The Autobiography of Benjamin Franklin',
            'author' => 'Benjamin Franklin',
            'category' => 'Biography',
            'description' => 'The life story of one of America\'s founding fathers.',
            'price' => 249.00,
            'stock' => 11,
            'rating' => 4.5,
            'isbn' => 'ISBN-978-0486290737',
            'publisher' => 'Dover Publications',
            'published_date' => '1791',
            'pages' => 148
        ],
        [
            'title' => 'Wings of Fire',
            'author' => 'A.P.J. Abdul Kalam',
            'category' => 'Biography',
            'description' => 'An autobiography of India\'s Missile Man and former President.',
            'price' => 299.00,
            'stock' => 14,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-8173711466',
            'publisher' => 'Universities Press',
            'published_date' => '1999',
            'pages' => 196
        ],

        // HISTORY (6 books)
        [
            'title' => 'Sapiens',
            'author' => 'Yuval Noah Harari',
            'category' => 'History',
            'description' => 'A brief history of humankind from the Stone Age to the modern age.',
            'price' => 599.00,
            'stock' => 12,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0062316097',
            'publisher' => 'Harper',
            'published_date' => '2011',
            'pages' => 443
        ],
        [
            'title' => 'Guns, Germs, and Steel',
            'author' => 'Jared Diamond',
            'category' => 'History',
            'description' => 'An exploration of why some civilizations succeeded while others failed.',
            'price' => 525.00,
            'stock' => 10,
            'rating' => 4.5,
            'isbn' => 'ISBN-978-0393317558',
            'publisher' => 'W. W. Norton',
            'published_date' => '1997',
            'pages' => 480
        ],
        [
            'title' => 'A People\'s History of the United States',
            'author' => 'Howard Zinn',
            'category' => 'History',
            'description' => 'American history told from the viewpoint of ordinary people rather than leaders.',
            'price' => 475.00,
            'stock' => 9,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-0060838652',
            'publisher' => 'Harper Perennial',
            'published_date' => '1980',
            'pages' => 729
        ],
        [
            'title' => 'The History of Ancient India',
            'author' => 'Romila Thapar',
            'category' => 'History',
            'description' => 'A comprehensive account of ancient Indian civilization.',
            'price' => 399.00,
            'stock' => 11,
            'rating' => 4.4,
            'isbn' => 'ISBN-978-0140138351',
            'publisher' => 'Penguin Books',
            'published_date' => '1966',
            'pages' => 384
        ],
        [
            'title' => 'The Second World War',
            'author' => 'Winston Churchill',
            'category' => 'History',
            'description' => 'A six-volume memoir of WWII by the British Prime Minister.',
            'price' => 799.00,
            'stock' => 6,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0395410561',
            'publisher' => 'Houghton Mifflin',
            'published_date' => '1948',
            'pages' => 1056
        ],
        [
            'title' => 'India After Gandhi',
            'author' => 'Ramachandra Guha',
            'category' => 'History',
            'description' => 'The history of the world\'s largest democracy since independence.',
            'price' => 699.00,
            'stock' => 8,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-0060958589',
            'publisher' => 'Ecco',
            'published_date' => '2007',
            'pages' => 893
        ],

        // SELF-HELP (6 books)
        [
            'title' => 'Atomic Habits',
            'author' => 'James Clear',
            'category' => 'Self-Help',
            'description' => 'A practical guide to building good habits and breaking bad ones.',
            'price' => 499.00,
            'stock' => 15,
            'rating' => 4.8,
            'isbn' => 'ISBN-978-0735211292',
            'publisher' => 'Avery',
            'published_date' => '2018',
            'pages' => 320
        ],
        [
            'title' => 'The 7 Habits of Highly Effective People',
            'author' => 'Stephen Covey',
            'category' => 'Self-Help',
            'description' => 'A principle-centered approach for solving personal and professional problems.',
            'price' => 549.00,
            'stock' => 12,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-1982137274',
            'publisher' => 'Simon & Schuster',
            'published_date' => '1989',
            'pages' => 381
        ],
        [
            'title' => 'How to Win Friends and Influence People',
            'author' => 'Dale Carnegie',
            'category' => 'Self-Help',
            'description' => 'Timeless advice on improving interpersonal relationships and communication.',
            'price' => 399.00,
            'stock' => 14,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-0671027032',
            'publisher' => 'Pocket Books',
            'published_date' => '1936',
            'pages' => 288
        ],
        [
            'title' => 'Think and Grow Rich',
            'author' => 'Napoleon Hill',
            'category' => 'Self-Help',
            'description' => 'The classic personal development and self-improvement book.',
            'price' => 299.00,
            'stock' => 13,
            'rating' => 4.5,
            'isbn' => 'ISBN-978-1585424337',
            'publisher' => 'TarcherPerigee',
            'published_date' => '1937',
            'pages' => 238
        ],
        [
            'title' => 'The Power of Now',
            'author' => 'Eckhart Tolle',
            'category' => 'Self-Help',
            'description' => 'A guide to spiritual enlightenment and living in the present moment.',
            'price' => 425.00,
            'stock' => 11,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-1577314806',
            'publisher' => 'New World Library',
            'published_date' => '1997',
            'pages' => 236
        ],
        [
            'title' => 'The Subtle Art of Not Giving a F*ck',
            'author' => 'Mark Manson',
            'category' => 'Self-Help',
            'description' => 'A counterintuitive approach to living a good life.',
            'price' => 449.00,
            'stock' => 14,
            'rating' => 4.5,
            'isbn' => 'ISBN-978-0062457714',
            'publisher' => 'HarperOne',
            'published_date' => '2016',
            'pages' => 224
        ],

        // TECHNOLOGY (6 books)
        [
            'title' => 'Clean Code',
            'author' => 'Robert C. Martin',
            'category' => 'Technology',
            'description' => 'A handbook of agile software craftsmanship.',
            'price' => 599.00,
            'stock' => 10,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0132350884',
            'publisher' => 'Prentice Hall',
            'published_date' => '2008',
            'pages' => 464
        ],
        [
            'title' => 'The Pragmatic Programmer',
            'author' => 'Andrew Hunt',
            'category' => 'Technology',
            'description' => 'Your journey to mastery in software development.',
            'price' => 649.00,
            'stock' => 9,
            'rating' => 4.8,
            'isbn' => 'ISBN-978-0135957059',
            'publisher' => 'Addison-Wesley',
            'published_date' => '2019',
            'pages' => 352
        ],
        [
            'title' => 'Artificial Intelligence: A Modern Approach',
            'author' => 'Stuart Russell',
            'category' => 'Technology',
            'description' => 'The leading textbook in Artificial Intelligence.',
            'price' => 799.00,
            'stock' => 7,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-0134610993',
            'publisher' => 'Pearson',
            'published_date' => '2020',
            'pages' => 1136
        ],
        [
            'title' => 'Introduction to Algorithms',
            'author' => 'Thomas H. Cormen',
            'category' => 'Technology',
            'description' => 'A comprehensive textbook on computer algorithms.',
            'price' => 899.00,
            'stock' => 6,
            'rating' => 4.7,
            'isbn' => 'ISBN-978-0262033848',
            'publisher' => 'MIT Press',
            'published_date' => '2009',
            'pages' => 1312
        ],
        [
            'title' => 'Design Patterns',
            'author' => 'Erich Gamma',
            'category' => 'Technology',
            'description' => 'Elements of reusable object-oriented software.',
            'price' => 699.00,
            'stock' => 8,
            'rating' => 4.6,
            'isbn' => 'ISBN-978-0201633610',
            'publisher' => 'Addison-Wesley',
            'published_date' => '1994',
            'pages' => 395
        ],
        [
            'title' => 'The Phoenix Project',
            'author' => 'Gene Kim',
            'category' => 'Technology',
            'description' => 'A novel about IT, DevOps, and helping your business win.',
            'price' => 549.00,
            'stock' => 10,
            'rating' => 4.5,
            'isbn' => 'ISBN-978-0988262508',
            'publisher' => 'IT Revolution Press',
            'published_date' => '2013',
            'pages' => 345
        ]
    ];

    // Prepare SQL statement
    $stmt = $conn->prepare("INSERT INTO books (title, author, category, description, price, stock, rating, isbn, publisher, published_date, pages) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

    if (!$stmt) {
        respond(false, 'Failed to prepare statement: ' . $conn->error);
    }

    // Insert each book
    foreach ($books as $book) {
        $stmt->bind_param(
            "ssssdidsssi",
            $book['title'],
            $book['author'],
            $book['category'],
            $book['description'],
            $book['price'],
            $book['stock'],
            $book['rating'],
            $book['isbn'],
            $book['publisher'],
            $book['published_date'],
            $book['pages']
        );

        if ($stmt->execute()) {
            $booksAdded++;
        } else {
            $errors[] = "Failed to add: " . $book['title'] . " - " . $stmt->error;
        }
    }

    $stmt->close();
    $conn->close();

    // Get category breakdown
    $conn = getConnection();
    $result = $conn->query("SELECT category, COUNT(*) as count FROM books GROUP BY category ORDER BY category");
    $categoryBreakdown = [];
    while ($row = $result->fetch_assoc()) {
        $categoryBreakdown[$row['category']] = (int) $row['count'];
    }
    $conn->close();

    respond(true, "$booksAdded books added successfully!", [
        'books_added' => $booksAdded,
        'category_breakdown' => $categoryBreakdown,
        'errors' => $errors
    ]);

} catch (Exception $e) {
    error_log("Populate Books Error: " . $e->getMessage());
    respond(false, 'Error: ' . $e->getMessage());
}
?>