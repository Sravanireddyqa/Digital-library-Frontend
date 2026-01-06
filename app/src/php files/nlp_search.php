<?php
/**
 * NLP Search Books API - Enhanced Version
 * Natural Language Processing for book search
 * Now with better matching and fallback results
 */

require_once 'db.php';

setHeaders();

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    respond(false, 'Method not allowed');
}

try {
    $query = isset($_GET['query']) ? strtolower(trim($_GET['query'])) : '';

    if (empty($query)) {
        respond(false, 'Search query is required');
    }

    $conn = getConnection();

    // NLP Processing
    $nlpResult = processNaturalLanguage($query);

    // First try: NLP-based search
    $sql = buildSearchQuery($nlpResult, $conn);
    $result = $conn->query($sql);

    $books = [];
    if ($result) {
        while ($row = $result->fetch_assoc()) {
            $books[] = formatBook($row);
        }
    }

    // If category was detected, DO NOT do fallback searches
    // This ensures dashboard count matches search count exactly
    if ($nlpResult['category']) {
        // Only try categoryFallback if no results and category detected
        if (empty($books)) {
            $books = categoryFallback($nlpResult['category'], $conn);
            $nlpResult['summary'] = "Showing " . $nlpResult['category'] . " books";
        }
    } else {
        // No category detected - use broader search
        if (empty($books) && !empty($nlpResult['keywords'])) {
            $books = broadSearch($nlpResult['keywords'], $conn);
            $nlpResult['summary'] = "Broadly searching for: " . implode(', ', $nlpResult['keywords']);
        }

        // Ultimate fallback: show all books
        if (empty($books)) {
            $books = getAllBooks($conn);
            $nlpResult['summary'] = "Showing all available books";
        }
    }

    // Sort by relevance
    if ($nlpResult['intent'] == 'recommend' || $nlpResult['sort'] == 'popular') {
        usort($books, function ($a, $b) {
            return $b['rating'] <=> $a['rating'];
        });
    }

    respond(true, 'Search completed', [
        'books' => $books,
        'count' => count($books),
        'nlp' => [
            'intent' => $nlpResult['intent'],
            'category' => $nlpResult['category'],
            'keywords' => $nlpResult['keywords'],
            'understood_as' => $nlpResult['summary']
        ]
    ]);

    $conn->close();

} catch (Exception $e) {
    error_log("NLP Search Error: " . $e->getMessage());
    respond(false, 'Server error: ' . $e->getMessage());
}

function formatBook($row)
{
    return [
        'id' => (int) $row['id'],
        'title' => $row['title'] ?? 'Unknown',
        'author' => $row['author'] ?? 'Unknown',
        'category' => $row['category'] ?? '',
        'isbn' => $row['isbn'] ?? '',
        'cover_url' => $row['cover_url'] ?? '',
        'rating' => (float) ($row['rating'] ?? 4.5),
        'price' => (float) ($row['price'] ?? 0),
        'available' => (int) ($row['available'] ?? 1),
        'description' => $row['description'] ?? ''
    ];
}

function getAllBooks($conn)
{
    $sql = "SELECT * FROM books ORDER BY rating DESC LIMIT 20";
    $result = $conn->query($sql);
    $books = [];
    if ($result) {
        while ($row = $result->fetch_assoc()) {
            $books[] = formatBook($row);
        }
    }
    return $books;
}

function broadSearch($keywords, $conn)
{
    $conditions = [];
    foreach ($keywords as $kw) {
        $kw = $conn->real_escape_string($kw);
        $conditions[] = "(
            LOWER(title) LIKE '%$kw%' OR 
            LOWER(author) LIKE '%$kw%' OR 
            LOWER(description) LIKE '%$kw%' OR
            LOWER(category) LIKE '%$kw%' OR
            LOWER(isbn) LIKE '%$kw%'
        )";
    }

    $sql = "SELECT * FROM books WHERE " . implode(" OR ", $conditions) . " ORDER BY rating DESC LIMIT 20";
    $result = $conn->query($sql);

    $books = [];
    if ($result) {
        while ($row = $result->fetch_assoc()) {
            $books[] = formatBook($row);
        }
    }
    return $books;
}

function categoryFallback($category, $conn)
{
    $cat = $conn->real_escape_string($category);
    // Use exact match for consistent counts with dashboard
    $sql = "SELECT * FROM books WHERE LOWER(category) = '" . strtolower($cat) . "' ORDER BY rating DESC LIMIT 50";
    $result = $conn->query($sql);

    $books = [];
    if ($result) {
        while ($row = $result->fetch_assoc()) {
            $books[] = formatBook($row);
        }
    }
    return $books;
}

/**
 * Process natural language query
 */
function processNaturalLanguage($query)
{
    $result = [
        'intent' => 'search',
        'category' => null,
        'author' => null,
        'keywords' => [],
        'sort' => 'default',
        'summary' => ''
    ];

    // Intent detection
    $recommendPatterns = ['recommend', 'suggest', 'give me', 'show me', 'i want', 'looking for', 'find me', 'any good', 'need'];
    foreach ($recommendPatterns as $pattern) {
        if (strpos($query, $pattern) !== false) {
            $result['intent'] = 'recommend';
            break;
        }
    }

    // Enhanced category detection - check LONGER phrases FIRST
    // Order matters: "science fiction" must be checked BEFORE "fiction"
    $categories = [
        // Check compound categories first
        'Science Fiction' => ['science fiction', 'sci-fi', 'scifi', 'sci fi'],
        'Non-Fiction' => ['non-fiction', 'nonfiction', 'non fiction'],
        'Self-Help' => ['self-help', 'self help', 'personal development'],
        // Programming/Technology - specific languages
        'Programming' => ['python', 'java', 'javascript', 'c programming', 'c++', 'cpp', 'coding', 'web development'],
        // Then single word categories
        'Fantasy' => ['fantasy', 'fantasies', 'magical', 'magic'],
        'Fiction' => ['fiction', 'novel', 'novels', 'story', 'stories', 'fictional', 'narrative'],
        'Mythology' => ['mythology', 'myth', 'myths', 'mythological', 'legend', 'legends', 'epic', 'epics', 'ramayana', 'mahabharata'],
        'History' => ['history', 'historical', 'past', 'ancient', 'medieval', 'civilization', 'heritage'],
        'Science' => ['science', 'scientific', 'physics', 'chemistry', 'biology'],
        'Technology' => ['technology', 'tech', 'engineering', 'computer', 'programming'],
        'Mystery' => ['mystery', 'detective', 'crime'],
        'Thriller' => ['thriller', 'suspense', 'scary'],
        'Horror' => ['horror', 'scary', 'frightening'],
        'Romance' => ['romance', 'romantic', 'love story'],
        'Biography' => ['biography', 'autobiography', 'memoir'],
        'Educational' => ['educational', 'education', 'learning', 'study', 'academic', 'textbook'],
        'Business' => ['business', 'finance', 'economics', 'money', 'investment', 'startup', 'entrepreneur', 'management'],
        'Data Science' => ['data science', 'machine learning', 'ai', 'artificial intelligence', 'deep learning']
    ];

    foreach ($categories as $category => $keywords) {
        foreach ($keywords as $keyword) {
            if (strpos($query, $keyword) !== false) {
                $result['category'] = $category;
                break 2;
            }
        }
    }

    // Author detection
    $authorPatterns = ['by ', 'written by ', 'author ', 'from '];
    foreach ($authorPatterns as $pattern) {
        $pos = strpos($query, $pattern);
        if ($pos !== false) {
            $afterPattern = substr($query, $pos + strlen($pattern));
            $words = explode(' ', trim($afterPattern));
            $authorWords = [];
            foreach ($words as $word) {
                if (in_array($word, ['about', 'with', 'and', 'or', 'the', 'in', 'on', 'for'])) {
                    break;
                }
                $authorWords[] = $word;
                if (count($authorWords) >= 3)
                    break;
            }
            if (!empty($authorWords)) {
                $result['author'] = implode(' ', $authorWords);
            }
            break;
        }
    }

    // Sort detection
    if (preg_match('/(popular|best|top|highly rated|famous|recommended)/i', $query)) {
        $result['sort'] = 'popular';
    }
    if (preg_match('/(new|latest|recent|newest)/i', $query)) {
        $result['sort'] = 'newest';
    }

    // Extract keywords (excluding common stop words)
    $stopWords = [
        'i',
        'me',
        'my',
        'want',
        'to',
        'read',
        'a',
        'an',
        'the',
        'some',
        'any',
        'book',
        'books',
        'about',
        'on',
        'for',
        'of',
        'and',
        'or',
        'please',
        'can',
        'you',
        'show',
        'give',
        'find',
        'recommend',
        'suggest',
        'good',
        'great',
        'nice',
        'with',
        'from',
        'by',
        'like',
        'similar',
        'need',
        'looking',
        'where',
        'how',
        'what',
        'is',
        'are',
        'was',
        'were',
        'be',
        'been'
    ];

    $words = preg_split('/\s+/', $query);
    foreach ($words as $word) {
        $cleanWord = preg_replace('/[^a-z0-9]/', '', $word);
        if (strlen($cleanWord) > 2 && !in_array($cleanWord, $stopWords)) {
            $result['keywords'][] = $cleanWord;
        }
    }

    // Generate summary
    $summary = "";
    if ($result['intent'] == 'recommend') {
        $summary = "Recommending ";
    } else {
        $summary = "Searching for ";
    }
    if ($result['sort'] == 'popular') {
        $summary .= "popular ";
    }
    if ($result['category']) {
        $summary .= $result['category'] . " ";
    }
    $summary .= "books";
    if ($result['author']) {
        $summary .= " by " . ucwords($result['author']);
    }
    if (!empty($result['keywords']) && !$result['category']) {
        $summary .= " about: " . implode(', ', $result['keywords']);
    }

    $result['summary'] = $summary;

    return $result;
}

/**
 * Build SQL query from NLP results
 */
function buildSearchQuery($nlp, $conn)
{
    $conditions = [];

    // Category filter - use exact match for consistent counts
    if ($nlp['category']) {
        $category = $conn->real_escape_string($nlp['category']);
        $conditions[] = "LOWER(category) = '" . strtolower($category) . "'";
    }

    // Author filter
    if ($nlp['author']) {
        $author = $conn->real_escape_string($nlp['author']);
        $conditions[] = "LOWER(author) LIKE '%$author%'";
    }

    // Keyword search - now more flexible with OR
    if (!empty($nlp['keywords'])) {
        $keywordConditions = [];
        foreach ($nlp['keywords'] as $keyword) {
            $kw = $conn->real_escape_string($keyword);
            $keywordConditions[] = "(
                LOWER(title) LIKE '%$kw%' OR 
                LOWER(author) LIKE '%$kw%' OR 
                LOWER(description) LIKE '%$kw%' OR
                LOWER(category) LIKE '%$kw%'
            )";
        }
        if (!empty($keywordConditions)) {
            $conditions[] = "(" . implode(" OR ", $keywordConditions) . ")";
        }
    }

    // Build final query
    $sql = "SELECT * FROM books";

    // If category is detected, ONLY filter by category for exact count matching
    if ($nlp['category']) {
        $category = $conn->real_escape_string($nlp['category']);
        $sql .= " WHERE LOWER(category) = '" . strtolower($category) . "'";

        // Only add author filter if specified (not keywords)
        if ($nlp['author']) {
            $author = $conn->real_escape_string($nlp['author']);
            $sql .= " AND LOWER(author) LIKE '%$author%'";
        }
    } else if (!empty($nlp['keywords']) || $nlp['author']) {
        // No category detected, use keywords and author with OR
        $conditions = [];

        if ($nlp['author']) {
            $author = $conn->real_escape_string($nlp['author']);
            $conditions[] = "LOWER(author) LIKE '%$author%'";
        }

        if (!empty($nlp['keywords'])) {
            $keywordConditions = [];
            foreach ($nlp['keywords'] as $keyword) {
                $kw = $conn->real_escape_string($keyword);
                $keywordConditions[] = "(
                    LOWER(title) LIKE '%$kw%' OR 
                    LOWER(author) LIKE '%$kw%' OR 
                    LOWER(description) LIKE '%$kw%' OR
                    LOWER(category) LIKE '%$kw%'
                )";
            }
            $conditions[] = "(" . implode(" OR ", $keywordConditions) . ")";
        }

        $sql .= " WHERE " . implode(" OR ", $conditions);
    }

    // Order by
    if ($nlp['sort'] == 'popular') {
        $sql .= " ORDER BY rating DESC, id DESC";
    } elseif ($nlp['sort'] == 'newest') {
        $sql .= " ORDER BY id DESC";
    } else {
        $sql .= " ORDER BY rating DESC, title ASC";
    }

    $sql .= " LIMIT 50";

    return $sql;
}
?>