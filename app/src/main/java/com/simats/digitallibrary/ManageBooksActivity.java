package com.simats.digitallibrary;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Manage Books Activity
 * Displays list of books with search, filter, add, edit, delete functionality
 */
public class ManageBooksActivity extends AppCompatActivity {

    private static final String TAG = "ManageBooksActivity";

    private ImageButton btnBack;
    private Button btnAddBook;
    private TextView tvBookCount;
    private EditText etSearch;
    private Spinner spinnerCategoryFilter, spinnerBookType;
    private RecyclerView recyclerBooks;
    private ProgressBar progressBar;
    private View layoutEmpty;

    private BookAdapter bookAdapter;
    private List<Book> allBooks = new ArrayList<>();
    private String currentFilter = "All Categories";
    private String currentBookType = "All Books";
    private String currentSearch = "";

    private static final String[] FILTER_CATEGORIES = {
            "All Categories",
            "Fiction",
            "Non-Fiction",
            "Mystery",
            "Science Fiction",
            "Romance",
            "Horror",
            "Biography",
            "History",
            "Science",
            "Self-Help"
    };

    private static final String[] BOOK_TYPES = {
            "All Books",
            "Old Books",
            "New Books"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_books);

        initViews();
        setupRecyclerView();
        setupCategoryFilter();
        setupListeners();
        loadBooks();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddBook = findViewById(R.id.btnAddBook);
        tvBookCount = findViewById(R.id.tvBookCount);
        etSearch = findViewById(R.id.etSearch);
        spinnerCategoryFilter = findViewById(R.id.spinnerCategoryFilter);
        spinnerBookType = findViewById(R.id.spinnerBookType);
        recyclerBooks = findViewById(R.id.recyclerBooks);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        bookAdapter = new BookAdapter();
        bookAdapter.setOnBookActionListener(new BookAdapter.OnBookActionListener() {
            @Override
            public void onEditClick(Book book) {
                showEditBookDialog(book);
            }

            @Override
            public void onDeleteClick(Book book) {
                showDeleteConfirmation(book);
            }
        });

        recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        recyclerBooks.setAdapter(bookAdapter);
    }

    private void setupCategoryFilter() {
        // Category filter
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                FILTER_CATEGORIES);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoryFilter.setAdapter(categoryAdapter);

        spinnerCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = FILTER_CATEGORIES[position];
                filterBooks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Book Type filter (Old/New)
        ArrayAdapter<String> bookTypeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                BOOK_TYPES);
        bookTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBookType.setAdapter(bookTypeAdapter);

        spinnerBookType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentBookType = BOOK_TYPES[position];
                filterBooks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddBook.setOnClickListener(v -> showAddBookDialog());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().toLowerCase().trim();
                filterBooks();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void showAddBookDialog() {
        AddBookDialogFragment dialog = new AddBookDialogFragment();
        dialog.setOnBookAddedListener(() -> {
            Toast.makeText(this, "Book added!", Toast.LENGTH_SHORT).show();
            loadBooks();
        });
        dialog.show(getSupportFragmentManager(), "AddBookDialog");
    }

    private void showEditBookDialog(Book book) {
        EditBookDialogFragment dialog = new EditBookDialogFragment();
        dialog.setBook(book);
        dialog.setOnBookUpdatedListener(() -> {
            Toast.makeText(this, "Book updated!", Toast.LENGTH_SHORT).show();
            loadBooks();
        });
        dialog.show(getSupportFragmentManager(), "EditBookDialog");
    }

    private void loadBooks() {
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
                            allBooks.clear();

                            for (int i = 0; i < booksArray.length(); i++) {
                                JSONObject obj = booksArray.getJSONObject(i);
                                Book book = new Book();
                                book.setId(obj.getInt("id"));
                                book.setTitle(obj.getString("title"));
                                book.setAuthor(obj.optString("author", "Unknown"));
                                book.setIsbn(obj.optString("isbn", ""));
                                book.setCategory(obj.optString("category", "Uncategorized"));
                                book.setPrice(obj.optDouble("price", 0));
                                book.setStock(obj.optInt("stock", 0));
                                book.setRating(obj.optDouble("rating", 0));
                                book.setCoverUrl(obj.optString("cover_url", "")); // Parse cover_url from API
                                book.setPages(obj.optInt("pages", 0)); // Parse pages from API
                                book.setDescription(obj.optString("description", "")); // Parse description from API
                                book.setPublisher(obj.optString("publisher", "")); // Parse publisher from API
                                book.setPublishedDate(obj.optString("published_date", "")); // Parse published_date from
                                                                                            // API

                                // Parse created_at for New/Old book filtering
                                String createdAtStr = obj.optString("created_at", "");
                                if (!createdAtStr.isEmpty()) {
                                    try {
                                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                                                "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                                        java.util.Date date = sdf.parse(createdAtStr);
                                        if (date != null) {
                                            book.setCreatedAt(date.getTime());
                                        }
                                    } catch (Exception e) {
                                        // If parsing fails, use 0 (will be treated as old book)
                                        book.setCreatedAt(0);
                                    }
                                } else {
                                    // No created_at means old book
                                    book.setCreatedAt(0);
                                }

                                // Parse is_new flag for Old/New book filtering
                                book.setNew(obj.optInt("is_new", 0) == 1);

                                allBooks.add(book);
                            }

                            filterBooks();
                        } else {
                            showEmpty();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                        showEmpty();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading books: " + error.getMessage());
                    showEmpty();
                    Toast.makeText(this, "Failed to load books", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void filterBooks() {
        List<Book> filtered = new ArrayList<>();

        for (Book book : allBooks) {
            boolean matchesCategory = currentFilter.equals("All Categories") ||
                    (book.getCategory() != null && book.getCategory().equalsIgnoreCase(currentFilter));

            boolean matchesSearch = currentSearch.isEmpty() ||
                    (book.getTitle() != null && book.getTitle().toLowerCase().contains(currentSearch)) ||
                    (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(currentSearch));

            // Book type filter based on is_new flag
            boolean matchesBookType = true;
            if (currentBookType.equals("New Books")) {
                // New books = added via Add Book button (is_new = 1)
                matchesBookType = book.isNew();
            } else if (currentBookType.equals("Old Books")) {
                // Old books = existing books (is_new = 0)
                matchesBookType = !book.isNew();
            }

            if (matchesCategory && matchesSearch && matchesBookType) {
                filtered.add(book);
            }
        }

        bookAdapter.setBooks(filtered);
        tvBookCount.setText(filtered.size() + " books in catalog");

        if (filtered.isEmpty()) {
            showEmpty();
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerBooks.setVisibility(View.VISIBLE);
        }
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerBooks.setVisibility(View.GONE);
    }

    private void showDeleteConfirmation(Book book) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Book")
                .setMessage("Are you sure you want to delete \"" + book.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteBook(book))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteBook(Book book) {
        JSONObject data = new JSONObject();
        try {
            data.put("book_id", book.getId());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_DELETE_BOOK,
                data,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Book deleted", Toast.LENGTH_SHORT).show();
                            bookAdapter.removeBook(book);
                            allBooks.remove(book);
                            tvBookCount.setText(allBooks.size() + " books in catalog");
                            if (allBooks.isEmpty()) {
                                showEmpty();
                            }
                        } else {
                            Toast.makeText(this, response.optString("message", "Delete failed"), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
