package com.simats.digitallibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Smart Search Books Activity with NLP
 * Supports natural language queries like:
 * - "recommend me fiction books"
 * - "books by Indian authors"
 * - "I want to read about mythology"
 * - "popular history books"
 */
public class SearchBooksActivity extends AppCompatActivity {

    private static final String TAG = "NLPSearch";
    private static final String PREF_NAME = "UserSession";

    private EditText etSearch;
    private RecyclerView recyclerBooks;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private TextView tvResultsCount, tvEmptyMessage, tvNlpHint;

    private RecommendedBookAdapter adapter;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Example queries for suggestions
    private static final String[] EXAMPLE_QUERIES = {
            "recommend me fiction novels",
            "books about mythology",
            "popular history books",
            "I want to read science fiction",
            "books by Indian authors",
            "best mystery thrillers",
            "educational books for learning",
            "romantic novels"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_books);

        initViews();
        setupRecyclerView();
        setupSearch();

        // Check if category filter passed
        String category = getIntent().getStringExtra("category");
        if (category != null && !category.isEmpty()) {
            etSearch.setText(category + " books");
            performNLPSearch(category + " books");
        } else {
            // Load all books by default
            loadAllBooks();
        }
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        recyclerBooks = findViewById(R.id.recyclerBooks);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvResultsCount = findViewById(R.id.tvResultsCount);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Update hint for NLP
        etSearch.setHint("Try: \"recommend me fiction books\"");

        // Setup bottom navigation
        NavigationHelper.setupBottomNavigation(this, R.id.nav_search);
    }

    private void setupRecyclerView() {
        adapter = new RecommendedBookAdapter();
        adapter.setOnBookClickListener(new RecommendedBookAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(Book book) {
                showBookDetails(book);
            }

            @Override
            public void onReserveClick(Book book) {
                reserveBook(book);
            }
        });

        recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        recyclerBooks.setNestedScrollingEnabled(false);
        recyclerBooks.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Debounce search - wait 500ms after typing stops
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                String query = s.toString().trim();
                if (query.length() >= 3) {
                    searchRunnable = () -> performNLPSearch(query);
                    searchHandler.postDelayed(searchRunnable, 500);
                } else if (query.isEmpty()) {
                    showSuggestions();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    performNLPSearch(query);
                }
                return true;
            }
            return false;
        });
    }

    private void loadAllBooks() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        recyclerBooks.setVisibility(View.GONE);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_GET_BOOKS,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray booksArray = response.getJSONArray("books");
                            List<Book> books = new ArrayList<>();

                            for (int i = 0; i < booksArray.length(); i++) {
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

                            tvResultsCount.setText("📚 All Books (" + books.size() + ")");
                            adapter.setBooks(books);
                            recyclerBooks.setVisibility(View.VISIBLE);
                        } else {
                            showSuggestions();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                        showSuggestions();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error: " + error.getMessage());
                    showSuggestions();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void showSuggestions() {
        tvResultsCount.setText("🤖 AI-Powered Search");
        tvEmptyMessage.setText("Try asking naturally:\n\n" +
                "• \"Recommend me fiction books\"\n" +
                "• \"Books about Indian mythology\"\n" +
                "• \"Popular science books\"\n" +
                "• \"I want to read history\"\n" +
                "• \"Mystery novels by famous authors\"");
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerBooks.setVisibility(View.GONE);
    }

    private void performNLPSearch(String query) {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        recyclerBooks.setVisibility(View.GONE);

        String encodedQuery;
        try {
            encodedQuery = URLEncoder.encode(query, "UTF-8");
        } catch (Exception e) {
            encodedQuery = query.replace(" ", "%20");
        }

        String url = ApiConfig.URL_NLP_SEARCH + "?query=" + encodedQuery;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray booksArray = response.getJSONArray("books");
                            List<Book> books = new ArrayList<>();

                            for (int i = 0; i < booksArray.length(); i++) {
                                JSONObject obj = booksArray.getJSONObject(i);
                                Book book = new Book();
                                book.setId(obj.getInt("id"));
                                book.setTitle(obj.optString("title", "Unknown"));
                                book.setAuthor(obj.optString("author", "Unknown"));
                                book.setCategory(obj.optString("category", ""));
                                book.setCoverUrl(obj.optString("cover_url", ""));
                                book.setIsbn(obj.optString("isbn", ""));
                                book.setRating(obj.optDouble("rating", 4.5));
                                book.setDescription(obj.optString("description", ""));
                                book.setAvailable(obj.optInt("available", 1) > 0);
                                book.setPrice(obj.optDouble("price", 0));
                                books.add(book);
                            }

                            // Show NLP understanding
                            if (response.has("nlp")) {
                                JSONObject nlp = response.getJSONObject("nlp");
                                String understood = nlp.optString("understood_as", "");
                                if (!understood.isEmpty()) {
                                    tvResultsCount.setText("🤖 " + understood);
                                }
                            }

                            if (books.isEmpty()) {
                                showNoResults(query);
                            } else {
                                tvResultsCount.setText("🤖 Found " + books.size() + " books");
                                adapter.setBooks(books);
                                recyclerBooks.setVisibility(View.VISIBLE);
                            }
                        } else {
                            showNoResults(query);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                        // Fallback to basic search
                        performBasicSearch(query);
                    }
                },
                error -> {
                    Log.e(TAG, "NLP Search error: " + error.getMessage());
                    // Fallback to basic search
                    performBasicSearch(query);
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void performBasicSearch(String query) {
        progressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_GET_BOOKS,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray booksArray = response.getJSONArray("books");
                            List<Book> books = new ArrayList<>();
                            String q = query.toLowerCase();

                            for (int i = 0; i < booksArray.length(); i++) {
                                JSONObject obj = booksArray.getJSONObject(i);
                                String title = obj.optString("title", "").toLowerCase();
                                String author = obj.optString("author", "").toLowerCase();
                                String category = obj.optString("category", "").toLowerCase();
                                String desc = obj.optString("description", "").toLowerCase();

                                if (title.contains(q) || author.contains(q) ||
                                        category.contains(q) || desc.contains(q)) {
                                    Book book = new Book();
                                    book.setId(obj.getInt("id"));
                                    book.setTitle(obj.optString("title", "Unknown"));
                                    book.setAuthor(obj.optString("author", "Unknown"));
                                    book.setCategory(obj.optString("category", ""));
                                    book.setCoverUrl(obj.optString("cover_url", ""));
                                    book.setRating(obj.optDouble("rating", 4.5));
                                    book.setPrice(obj.optDouble("price", 0));
                                    books.add(book);
                                }
                            }

                            if (books.isEmpty()) {
                                showNoResults(query);
                            } else {
                                tvResultsCount.setText(books.size() + " books found");
                                adapter.setBooks(books);
                                recyclerBooks.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                        showNoResults(query);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    showNoResults(query);
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void showNoResults(String query) {
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerBooks.setVisibility(View.GONE);
        tvResultsCount.setText("No results");
        tvEmptyMessage.setText("No books match \"" + query + "\"\n\nTry:\n" +
                "• Different keywords\n" +
                "• Broader categories like \"fiction\" or \"history\"\n" +
                "• Author names");
    }

    private void showBookDetails(Book book) {
        Intent intent = new Intent(this, BookDetailsActivity.class);
        intent.putExtra("book_id", book.getId());
        startActivity(intent);
    }

    private void reserveBook(Book book) {
        // Open the multi-step reservation flow
        Intent intent = new Intent(this, ReservationFlowActivity.class);
        intent.putExtra("book_id", book.getId());
        intent.putExtra("book_title", book.getTitle());
        intent.putExtra("book_author", book.getAuthor());
        intent.putExtra("book_cover", book.getCoverUrl());
        intent.putExtra("book_price", book.getPrice());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}
