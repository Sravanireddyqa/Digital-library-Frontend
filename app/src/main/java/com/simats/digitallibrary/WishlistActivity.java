package com.simats.digitallibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

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
 * Wishlist Activity with Remove functionality
 */
public class WishlistActivity extends AppCompatActivity {

    private static final String TAG = "Wishlist";
    private static final String PREF_NAME = "UserSession";

    private RecyclerView recyclerWishlist;
    private ProgressBar progressBar;
    private View layoutEmpty;

    private WishlistAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        initViews();
        setupRecyclerView();
        loadWishlist();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerWishlist = findViewById(R.id.recyclerWishlist);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        findViewById(R.id.btnBrowseBooks).setOnClickListener(v -> {
            startActivity(new Intent(this, SearchBooksActivity.class));
            finish();
        });

        // Setup bottom navigation
        NavigationHelper.setupBottomNavigation(this, R.id.nav_wishlist);
    }

    private void setupRecyclerView() {
        adapter = new WishlistAdapter();
        adapter.setOnWishlistActionListener(new WishlistAdapter.OnWishlistActionListener() {
            @Override
            public void onBookClick(Book book) {
                Intent intent = new Intent(WishlistActivity.this, BookDetailsActivity.class);
                intent.putExtra("book_id", book.getId());
                startActivity(intent);
            }

            @Override
            public void onReserveClick(Book book) {
                reserveBook(book);
            }

            @Override
            public void onRemoveClick(Book book, int position) {
                removeFromWishlist(book, position);
            }
        });

        recyclerWishlist.setLayoutManager(new LinearLayoutManager(this));
        recyclerWishlist.setNestedScrollingEnabled(false);
        recyclerWishlist.setAdapter(adapter);
    }

    private void loadWishlist() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_GET_WISHLIST + "?user_id=" + userId,
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
                                book.setCoverUrl(obj.optString("cover_url", ""));
                                book.setRating(obj.optDouble("rating", 4.5));
                                book.setPrice(obj.optDouble("price", 0));
                                books.add(book);
                            }

                            if (books.isEmpty()) {
                                showEmpty();
                            } else {
                                adapter.setBooks(books);
                                recyclerWishlist.setVisibility(View.VISIBLE);
                            }
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
                    showEmpty();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerWishlist.setVisibility(View.GONE);
    }

    private void removeFromWishlist(Book book, int position) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        // Optimistic UI update
        adapter.removeBook(position);
        Toast.makeText(this, "Removed from wishlist", Toast.LENGTH_SHORT).show();

        // Check if list is empty now
        if (adapter.getItemCount() == 0) {
            showEmpty();
        }

        // API call to remove
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", userId);
            data.put("book_id", book.getId());
            data.put("action", "remove");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_TOGGLE_WISHLIST,
                data,
                response -> {
                    // Already updated UI
                },
                error -> {
                    // Reload on error
                    Toast.makeText(this, "Error removing", Toast.LENGTH_SHORT).show();
                    loadWishlist();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
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
