package com.simats.digitallibrary;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
 * Orders Activity
 * Shows all book orders/reservations with user info
 * Clickable stat cards for filtering
 */
public class OrdersActivity extends AppCompatActivity {

    private static final String TAG = "OrdersActivity";

    private ImageButton btnBack;
    private TextView tvOrdersSubtitle, tvTotalOrders, tvPendingOrders, tvApprovedOrders;
    private View cardTotal, cardPending, cardApproved;
    private RecyclerView recyclerOrders;
    private ProgressBar progressBar;
    private View layoutEmpty;

    private OrderAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private String currentFilter = "all"; // "all", "pending", "approved", "rejected"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        initViews();
        setupRecyclerView();
        setupFilterListeners();
        loadOrders();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvOrdersSubtitle = findViewById(R.id.tvOrdersSubtitle);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
        tvApprovedOrders = findViewById(R.id.tvApprovedOrders);
        recyclerOrders = findViewById(R.id.recyclerOrders);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        // Get parent cards for click handling
        cardTotal = findViewById(R.id.cardTotalOrders);
        cardPending = findViewById(R.id.cardPending);
        cardApproved = findViewById(R.id.cardApproved);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new OrderAdapter();
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setNestedScrollingEnabled(true);
        recyclerOrders.setAdapter(adapter);
    }

    private void setupFilterListeners() {
        // Total Orders - show all
        if (cardTotal != null) {
            cardTotal.setOnClickListener(v -> {
                currentFilter = "all";
                filterOrders();
                highlightSelectedCard();
            });
        }

        // Pending Orders
        if (cardPending != null) {
            cardPending.setOnClickListener(v -> {
                currentFilter = "pending";
                filterOrders();
                highlightSelectedCard();
            });
        }

        // Approved Orders
        if (cardApproved != null) {
            cardApproved.setOnClickListener(v -> {
                currentFilter = "approved";
                filterOrders();
                highlightSelectedCard();
            });
        }
    }

    private void highlightSelectedCard() {
        // Reset all cards
        if (cardTotal != null)
            cardTotal.setAlpha(currentFilter.equals("all") ? 1.0f : 0.6f);
        if (cardPending != null)
            cardPending.setAlpha(currentFilter.equals("pending") ? 1.0f : 0.6f);
        if (cardApproved != null)
            cardApproved.setAlpha(currentFilter.equals("approved") ? 1.0f : 0.6f);
    }

    private void filterOrders() {
        List<Order> filtered = new ArrayList<>();

        for (Order order : allOrders) {
            if (currentFilter.equals("all")) {
                filtered.add(order);
            } else if (order.getStatus().toLowerCase().equals(currentFilter)) {
                filtered.add(order);
            }
        }

        if (filtered.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerOrders.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerOrders.setVisibility(View.VISIBLE);
            adapter.setOrders(filtered);
        }

        // Update subtitle
        String filterName = currentFilter.equals("all") ? "All"
                : currentFilter.substring(0, 1).toUpperCase() + currentFilter.substring(1);
        tvOrdersSubtitle.setText(filtered.size() + " " + filterName + " orders");
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_GET_RESERVATIONS,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray reservations = response.getJSONArray("reservations");
                            allOrders.clear();

                            int total = 0, pending = 0, approved = 0;

                            for (int i = 0; i < reservations.length(); i++) {
                                JSONObject obj = reservations.getJSONObject(i);
                                Order order = new Order();
                                order.setId(obj.getInt("id"));
                                order.setBookTitle(obj.optString("book_title", "Unknown Book"));
                                order.setUserName(obj.optString("user_name", "Unknown User"));
                                order.setUserEmail(obj.optString("user_email", ""));
                                order.setStatus(obj.optString("status", "pending"));
                                order.setOrderDate(obj.optString("pickup_date", obj.optString("created_at", "")));
                                allOrders.add(order);

                                total++;
                                String status = order.getStatus().toLowerCase();
                                if ("pending".equals(status))
                                    pending++;
                                if ("approved".equals(status))
                                    approved++;
                            }

                            // Update stats
                            tvTotalOrders.setText(String.valueOf(total));
                            tvPendingOrders.setText(String.valueOf(pending));
                            tvApprovedOrders.setText(String.valueOf(approved));

                            // Apply current filter
                            filterOrders();
                            highlightSelectedCard();

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
                    Log.e(TAG, "Error: " + error.getMessage());
                    showEmpty();
                    Toast.makeText(this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerOrders.setVisibility(View.GONE);
    }
}
