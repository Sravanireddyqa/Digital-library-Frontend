package com.simats.digitallibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Book Details Activity
 * Shows comprehensive book information with reserve, wishlist, availability,
 * and similar books
 */
public class BookDetailsActivity extends AppCompatActivity {

    private static final String TAG = "BookDetails";
    private static final String PREF_NAME = "UserSession";

    // Views
    private ImageView ivBookCover, ivWishlistIcon;
    private TextView tvBookTitle, tvAuthor, tvRating, tvCategory, tvPages, tvCopiesAvailable;
    private TextView tvDescription, tvPublisher, tvPublishedDate, tvIsbn, tvLanguage;
    private TextView btnReserve, tvWishlistText;
    private View btnAddWishlist;
    private RecyclerView recyclerLibraries, recyclerSimilarBooks;
    private BottomNavigationView bottomNavigation;

    // Adapters
    private LibraryAvailabilityAdapter libraryAdapter;
    private SimilarBooksAdapter similarBooksAdapter;

    // Data
    private int bookId;
    private Book currentBook;
    private boolean isInWishlist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        // Get book ID from intent
        bookId = getIntent().getIntExtra("book_id", 0);
        if (bookId == 0) {
            Toast.makeText(this, "Book not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerViews();
        setupClickListeners();
        setupBottomNavigation();
        loadBookDetails();
    }

    private void initViews() {
        // Header
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Top bar buttons - Notification and Profile
        findViewById(R.id.btnNotification).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });
        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ReaderProfileActivity.class));
        });

        // Book info
        ivBookCover = findViewById(R.id.ivBookCover);
        tvBookTitle = findViewById(R.id.tvBookTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvRating = findViewById(R.id.tvRating);
        tvCategory = findViewById(R.id.tvCategory);
        tvPages = findViewById(R.id.tvPages);
        tvCopiesAvailable = findViewById(R.id.tvCopiesAvailable);
        tvDescription = findViewById(R.id.tvDescription);
        tvPublisher = findViewById(R.id.tvPublisher);
        tvPublishedDate = findViewById(R.id.tvPublishedDate);
        tvIsbn = findViewById(R.id.tvIsbn);
        tvLanguage = findViewById(R.id.tvLanguage);

        // Buttons
        btnReserve = findViewById(R.id.btnReserve);
        btnAddWishlist = findViewById(R.id.btnAddWishlist);
        ivWishlistIcon = findViewById(R.id.ivWishlistIcon);
        tvWishlistText = findViewById(R.id.tvWishlistText);

        // RecyclerViews
        recyclerLibraries = findViewById(R.id.recyclerLibraries);
        recyclerSimilarBooks = findViewById(R.id.recyclerSimilarBooks);

        // Bottom Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerViews() {
        // Library availability list
        libraryAdapter = new LibraryAvailabilityAdapter();
        recyclerLibraries.setLayoutManager(new LinearLayoutManager(this));
        recyclerLibraries.setNestedScrollingEnabled(false);
        recyclerLibraries.setAdapter(libraryAdapter);

        // Similar books horizontal list
        similarBooksAdapter = new SimilarBooksAdapter();
        similarBooksAdapter.setOnBookClickListener(book -> {
            Intent intent = new Intent(this, BookDetailsActivity.class);
            intent.putExtra("book_id", book.getId());
            startActivity(intent);
        });
        recyclerSimilarBooks.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerSimilarBooks.setAdapter(similarBooksAdapter);
    }

    private void setupClickListeners() {
        btnReserve.setOnClickListener(v -> reserveBook());
        btnAddWishlist.setOnClickListener(v -> toggleWishlist());
    }

    private void setupBottomNavigation() {
        NavigationHelper.setupBottomNavigation(this, R.id.nav_home);
    }

    private void loadBookDetails() {
        String url = ApiConfig.URL_GET_BOOK_DETAILS + "?book_id=" + bookId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handleBookDetailsResponse,
                error -> {
                    Log.e(TAG, "Error loading book: " + error.getMessage());
                    // Load sample data as fallback
                    loadSampleData();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void handleBookDetailsResponse(JSONObject response) {
        try {
            if (response.getBoolean("success")) {
                JSONObject bookObj = response.getJSONObject("book");

                currentBook = new Book();
                currentBook.setId(bookObj.getInt("id"));
                currentBook.setTitle(bookObj.optString("title", "Unknown"));
                currentBook.setAuthor(bookObj.optString("author", "Unknown"));
                currentBook.setCategory(bookObj.optString("category", ""));
                currentBook.setDescription(bookObj.optString("description", ""));
                currentBook.setPublisher(bookObj.optString("publisher", "Unknown"));
                currentBook.setPublishedDate(bookObj.optString("published_date", ""));
                currentBook.setIsbn(bookObj.optString("isbn", ""));
                currentBook.setPages(bookObj.optInt("pages", 0));
                currentBook.setRating(bookObj.optDouble("rating", 4.5));
                currentBook.setCoverUrl(bookObj.optString("cover_url", ""));
                currentBook.setPrice(bookObj.optDouble("price", 299.0));
                currentBook.setStock(bookObj.optInt("stock", 10)); // Parse stock from API

                displayBookDetails();

                // Load libraries
                if (response.has("libraries")) {
                    JSONArray librariesArray = response.getJSONArray("libraries");
                    loadLibraryAvailability(librariesArray);
                } else {
                    loadSampleLibraries();
                }

                // Load similar books
                if (response.has("similar_books")) {
                    JSONArray similarArray = response.getJSONArray("similar_books");
                    loadSimilarBooks(similarArray);
                } else {
                    loadSampleSimilarBooks();
                }

                // Check wishlist status
                isInWishlist = response.optBoolean("in_wishlist", false);
                updateWishlistButton();

            } else {
                loadSampleData();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Parse error: " + e.getMessage());
            loadSampleData();
        }
    }

    private void displayBookDetails() {
        // Title & Author
        tvBookTitle.setText(currentBook.getTitle());
        tvAuthor.setText(currentBook.getAuthor());

        // Rating, Category, Pages
        tvRating.setText(String.valueOf(currentBook.getRating()));
        tvCategory.setText(currentBook.getCategory());
        tvPages.setText(currentBook.getPages() + " pages");

        // Copies available
        int stock = currentBook.getStock();
        if (stock > 0) {
            tvCopiesAvailable.setText(stock + " copies");
            tvCopiesAvailable.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvCopiesAvailable.setText("Out of stock");
            tvCopiesAvailable.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // Description
        String description = currentBook.getDescription();
        if (description != null && !description.isEmpty()) {
            tvDescription.setText(description);
        } else {
            tvDescription.setText("No description available for this book.");
        }

        // Book Details
        tvPublisher.setText(currentBook.getPublisher() != null ? currentBook.getPublisher() : "Unknown");
        tvPublishedDate.setText(currentBook.getPublishedDate() != null ? currentBook.getPublishedDate() : "N/A");
        tvIsbn.setText(currentBook.getIsbn() != null ? currentBook.getIsbn() : "N/A");
        tvLanguage.setText("EN"); // Default to English

        // Cover Image - handle relative URLs and fallbacks
        String coverUrl = currentBook.getCoverUrl();
        String isbn = currentBook.getIsbn();

        // Determine best cover URL
        String imageUrl = null;

        if (coverUrl != null && !coverUrl.isEmpty() && !coverUrl.equals("0")) {
            // If it's a relative path like "uploads/...", prepend base URL
            if (coverUrl.startsWith("uploads/") || !coverUrl.startsWith("http")) {
                imageUrl = ApiConfig.BASE_URL + coverUrl;
            } else {
                imageUrl = coverUrl;
            }
        }

        // If no valid stored URL, use Open Library with ISBN
        if ((imageUrl == null || imageUrl.isEmpty()) && isbn != null && !isbn.isEmpty()) {
            imageUrl = "https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg";
        }

        // Load the image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d("BookDetails", "Loading cover: " + imageUrl);
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_library_book)
                    .error(R.drawable.ic_library_book)
                    .fitCenter()
                    .into(ivBookCover);
        }
    }

    private void loadLibraryAvailability(JSONArray array) throws JSONException {
        List<LibraryAvailabilityAdapter.LibraryAvailability> libraries = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            libraries.add(new LibraryAvailabilityAdapter.LibraryAvailability(
                    obj.getInt("library_id"),
                    obj.getString("name"),
                    obj.optString("address", ""),
                    obj.optInt("available", 0)));
        }

        libraryAdapter.setLibraries(libraries);
    }

    private void loadSimilarBooks(JSONArray array) throws JSONException {
        List<Book> books = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            Book book = new Book();
            book.setId(obj.getInt("id"));
            book.setTitle(obj.optString("title", "Unknown"));
            book.setAuthor(obj.optString("author", "Unknown"));
            book.setCoverUrl(obj.optString("cover_url", ""));
            books.add(book);
        }

        similarBooksAdapter.setBooks(books);
    }

    private void loadSampleData() {
        // Create sample book
        currentBook = new Book();
        currentBook.setId(bookId);
        currentBook.setTitle("The God of Small Things");
        currentBook.setAuthor("Arundhati Roy");
        currentBook.setCategory("Fiction");
        currentBook.setDescription(
                "Set in Kerala, this Booker Prize-winning novel tells the story of fraternal twins whose lives are destroyed by the \"Love Laws\" that lay down \"who should be loved, and how. And how much.\" A powerful exploration of forbidden love, caste discrimination, and family secrets in post-colonial India.");
        currentBook.setPublisher("IndiaInk");
        currentBook.setPublishedDate("4/1/1997");
        currentBook.setIsbn("978-81-7223-300-4");
        currentBook.setPages(340);
        currentBook.setRating(4.8);
        currentBook.setPrice(299.0); // Book price in rupees

        displayBookDetails();
        loadSampleLibraries();
        loadSampleSimilarBooks();
    }

    private void loadSampleLibraries() {
        List<LibraryAvailabilityAdapter.LibraryAvailability> libraries = new ArrayList<>();
        // SIMATS Central Library (Chennai)
        libraries.add(new LibraryAvailabilityAdapter.LibraryAvailability(
                3, "SIMATS Central Library",
                "Saveetha University, Chennai 602105",
                "Mon-Sun: 8:00 AM - 10:00 PM", 8,
                13.0540, 80.0184));
        // Anna Centenary Library (Chennai)
        libraries.add(new LibraryAvailabilityAdapter.LibraryAvailability(
                4, "Anna Centenary Library",
                "Kotturpuram, Chennai 600025",
                "Mon-Sun: 9:00 AM - 8:00 PM", 6,
                13.0192, 80.2394));
        // Connemara Public Library (Chennai)
        libraries.add(new LibraryAvailabilityAdapter.LibraryAvailability(
                5, "Connemara Public Library",
                "Pantheon Road, Egmore, Chennai 600008",
                "Mon-Sat: 9:30 AM - 7:00 PM", 4,
                13.0724, 80.2610));
        // Bangalore Central Library
        libraries.add(new LibraryAvailabilityAdapter.LibraryAvailability(
                6, "Bangalore Central Library",
                "Cubbon Park, Bengaluru 560001",
                "Mon-Sat: 8:00 AM - 8:00 PM", 7,
                12.9752, 77.5912));

        libraryAdapter.setLibraries(libraries);
    }

    private void loadSampleSimilarBooks() {
        List<Book> books = new ArrayList<>();

        Book book1 = new Book();
        book1.setId(101);
        book1.setTitle("Midnight's Children");
        book1.setAuthor("Salman Rushdie");
        book1.setPrice(450.0);
        books.add(book1);

        Book book2 = new Book();
        book2.setId(102);
        book2.setTitle("The Inheritance of Loss");
        book2.setAuthor("Kiran Desai");
        book2.setPrice(399.0);
        books.add(book2);

        Book book3 = new Book();
        book3.setId(103);
        book3.setTitle("A Fine Balance");
        book3.setAuthor("Rohinton Mistry");
        book3.setPrice(525.0);
        books.add(book3);

        similarBooksAdapter.setBooks(books);
    }

    private void reserveBook() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        if (userId == 0) {
            Toast.makeText(this, "Please login to reserve", Toast.LENGTH_SHORT).show();
            return;
        }

        // Open reservation flow with book info
        Intent intent = new Intent(this, ReservationFlowActivity.class);
        intent.putExtra("book_id", bookId);
        intent.putExtra("book_title", currentBook != null ? currentBook.getTitle() : "Book");
        intent.putExtra("book_author", currentBook != null ? currentBook.getAuthor() : "Author");
        intent.putExtra("book_cover", currentBook != null ? currentBook.getCoverUrl() : "");
        intent.putExtra("book_price", currentBook != null ? currentBook.getPrice() : 0.0);
        startActivity(intent);
    }

    private void toggleWishlist() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        if (userId == 0) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            return;
        }

        // Toggle optimistically
        isInWishlist = !isInWishlist;
        updateWishlistButton();

        // Call API
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", userId);
            data.put("book_id", bookId);
            data.put("action", isInWishlist ? "add" : "remove");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_TOGGLE_WISHLIST,
                data,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            String message = isInWishlist ? "❤️ Added to wishlist" : "Removed from wishlist";
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Revert on error
                    isInWishlist = !isInWishlist;
                    updateWishlistButton();
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void updateWishlistButton() {
        if (isInWishlist) {
            ivWishlistIcon.setColorFilter(getResources().getColor(android.R.color.holo_red_light));
            tvWishlistText.setText("In Wishlist");
            tvWishlistText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        } else {
            ivWishlistIcon.setColorFilter(getResources().getColor(android.R.color.darker_gray));
            tvWishlistText.setText("Add to Wishlist");
            tvWishlistText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }
}
