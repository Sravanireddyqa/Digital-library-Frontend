package com.simats.digitallibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Reader Dashboard Activity with Dynamic Categories
 */
public class ReaderDashboardActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private static final String TAG = "ReaderDashboard";
    private static final String PREF_NAME = "UserSession";

    private TextView tvUserName;
    private TextView tvBooksReadCount, tvReservationsCount, tvWishlistCount;
    private RecyclerView recyclerRecommended;
    private BottomNavigationView bottomNavigation;
    private LinearLayout layoutCategories;

    private RecommendedBookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_dashboard);

        initViews();
        loadUserData();
        setupRecyclerView();
        setupBottomNavigation();
        setupClickListeners();
        loadDashboardData();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvBooksReadCount = findViewById(R.id.tvBooksReadCount);
        tvReservationsCount = findViewById(R.id.tvReservationsCount);
        tvWishlistCount = findViewById(R.id.tvWishlistCount);
        recyclerRecommended = findViewById(R.id.recyclerRecommended);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        layoutCategories = findViewById(R.id.layoutCategories);

        // AI Assistant FAB
        findViewById(R.id.fabAIAssistant).setOnClickListener(v -> {
            startActivity(new Intent(this, AIAssistantActivity.class));
        });
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String name = prefs.getString("name", "Reader");
        tvUserName.setText(name);
    }

    private void setupRecyclerView() {
        adapter = new RecommendedBookAdapter();
        adapter.setOnBookClickListener(new RecommendedBookAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(Book book) {
                Intent intent = new Intent(ReaderDashboardActivity.this, BookDetailsActivity.class);
                intent.putExtra("book_id", book.getId());
                startActivity(intent);
            }

            @Override
            public void onReserveClick(Book book) {
                reserveBook(book);
            }
        });

        recyclerRecommended.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecommended.setNestedScrollingEnabled(false);
        recyclerRecommended.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        NavigationHelper.setupBottomNavigation(this, R.id.nav_home);
    }

    private void setupClickListeners() {
        ImageButton btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ReaderProfileActivity.class));
        });

        ImageButton btnNotification = findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        findViewById(R.id.btnAIAssistant).setOnClickListener(v -> {
            Intent intent = new Intent(this, AIAssistantActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnBrowseLibraries).setOnClickListener(v -> {
            startActivity(new Intent(this, BrowseLibrariesActivity.class));
        });

        // Stats cards - pass mode to filter reservations
        findViewById(R.id.cardBooksRead).setOnClickListener(v -> {
            Intent intent = new Intent(this, MyBookingsActivity.class);
            intent.putExtra("mode", "read"); // Show only returned books
            startActivity(intent);
        });
        findViewById(R.id.cardReservations).setOnClickListener(v -> {
            Intent intent = new Intent(this, MyBookingsActivity.class);
            intent.putExtra("mode", "active"); // Show pending/approved only
            startActivity(intent);
        });
        findViewById(R.id.cardWishlist).setOnClickListener(v -> {
            startActivity(new Intent(this, WishlistActivity.class));
        });
    }

    private void openCategory(String category) {
        Intent intent = new Intent(this, SearchBooksActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }

    private void loadDashboardData() {
        loadRecommendedBooks();
        loadUserStats();
        loadCategoryCounts();
    }

    private void loadRecommendedBooks() {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_GET_BOOKS,
                null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray booksArray = response.getJSONArray("books");
                            List<Book> books = new ArrayList<>();

                            int limit = Math.min(booksArray.length(), 5);
                            for (int i = 0; i < limit; i++) {
                                JSONObject obj = booksArray.getJSONObject(i);
                                Book book = new Book();
                                book.setId(obj.getInt("id"));
                                book.setTitle(obj.getString("title"));
                                book.setAuthor(obj.optString("author", "Unknown"));
                                book.setCategory(obj.optString("category", ""));
                                book.setCoverUrl(obj.optString("cover_url", ""));
                                book.setRating(obj.optDouble("rating", 4.5));
                                book.setPrice(obj.optDouble("price", 0));
                                books.add(book);
                            }

                            adapter.setBooks(books);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                        loadSampleBooks();
                    }
                },
                error -> {
                    Log.e(TAG, "Error: " + error.getMessage());
                    loadSampleBooks();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadSampleBooks() {
        List<Book> books = new ArrayList<>();
        Book book1 = new Book();
        book1.setId(1);
        book1.setTitle("The God of Small Things");
        book1.setAuthor("Arundhati Roy");
        book1.setRating(4.8);
        book1.setPrice(299.0);
        books.add(book1);

        Book book2 = new Book();
        book2.setId(2);
        book2.setTitle("Clean Code");
        book2.setAuthor("Robert C. Martin");
        book2.setRating(4.7);
        book2.setPrice(599.0);
        books.add(book2);

        adapter.setBooks(books);
    }

    private void loadUserStats() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_GET_READER_STATS + "?user_id=" + userId,
                null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject stats = response.getJSONObject("stats");
                            tvBooksReadCount.setText(String.valueOf(stats.optInt("books_read", 0)));
                            tvReservationsCount.setText(String.valueOf(stats.optInt("active_reservations", 0)));
                            tvWishlistCount.setText(String.valueOf(stats.optInt("wishlist_items", 0)));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Stats parse error: " + e.getMessage());
                    }
                },
                error -> {
                    tvBooksReadCount.setText("0");
                    tvReservationsCount.setText("0");
                    tvWishlistCount.setText("0");
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadCategoryCounts() {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_GET_CATEGORIES,
                null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject categories = response.getJSONObject("categories");
                            int totalBooks = response.optInt("total_books", 0);
                            buildCategoryCards(categories, totalBooks);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Categories parse error: " + e.getMessage());
                        buildSampleCategories();
                    }
                },
                error -> {
                    Log.e(TAG, "Error loading categories: " + error.getMessage());
                    buildSampleCategories();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void buildCategoryCards(JSONObject categories, int totalBooks) {
        layoutCategories.removeAllViews();

        // Add "All Books" card first
        addCategoryCard("All Books", totalBooks, true);

        Iterator<String> keys = categories.keys();
        while (keys.hasNext()) {
            String categoryName = keys.next();
            int count = categories.optInt(categoryName, 0);
            addCategoryCard(categoryName, count, false);
        }
    }

    private void buildSampleCategories() {
        layoutCategories.removeAllViews();
        addCategoryCard("All Books", 500, true);
        addCategoryCard("Fiction", 50, false);
        addCategoryCard("Classic", 40, false);
        addCategoryCard("Technology", 60, false);
        addCategoryCard("Non-Fiction", 30, false);
        addCategoryCard("Sci-Fi", 4, false);
    }

    private void addCategoryCard(String categoryName, int bookCount, boolean isHighlighted) {
        // Create CardView
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                dpToPx(isHighlighted ? 140 : 120), LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, dpToPx(12), 0);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(16));
        cardView.setCardElevation(isHighlighted ? dpToPx(4) : 0);

        // All Books card gets purple background
        if (isHighlighted) {
            cardView.setCardBackgroundColor(0xFF7C3AED);
        } else {
            cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));
        }

        // Create inner layout
        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        innerLayout.setGravity(android.view.Gravity.CENTER);

        // Category name
        TextView tvName = new TextView(this);
        tvName.setText(categoryName);
        tvName.setTextSize(isHighlighted ? 15 : 14);
        tvName.setTextColor(isHighlighted ? 0xFFFFFFFF : 0xFF1F2937);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);

        // Book count
        TextView tvCount = new TextView(this);
        tvCount.setText(bookCount + " books");
        tvCount.setTextSize(12);
        tvCount.setTextColor(isHighlighted ? 0xFFE0E0FF : 0xFF6B7280);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        countParams.topMargin = dpToPx(4);
        tvCount.setLayoutParams(countParams);

        innerLayout.addView(tvName);
        innerLayout.addView(tvCount);
        cardView.addView(innerLayout);

        // Click listener - All Books shows all, others filter
        cardView.setOnClickListener(v -> {
            if (categoryName.equals("All Books")) {
                startActivity(new Intent(this, SearchBooksActivity.class));
            } else {
                openCategory(categoryName);
            }
        });

        layoutCategories.addView(cardView);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void reserveBook(Book book) {
        Intent intent = new Intent(this, ReservationFlowActivity.class);
        intent.putExtra("book_id", book.getId());
        intent.putExtra("book_title", book.getTitle());
        intent.putExtra("book_author", book.getAuthor());
        intent.putExtra("book_cover", book.getCoverUrl());
        intent.putExtra("book_price", book.getPrice());
        startActivity(intent);
    }
}
