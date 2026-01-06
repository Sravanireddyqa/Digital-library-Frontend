package com.simats.digitallibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;

/**
 * Admin Dashboard Activity
 * Main screen for admin users after login
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private static final String PREF_NAME = "UserSession";

    private BottomNavigationView bottomNavigation;
    private FrameLayout btnNotification;
    private RecyclerView recyclerActivity;
    private ProgressBar progressActivity;
    private TextView tvNoActivity;
    private ActivityAdapter activityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        setupListeners();
        setupRecyclerView();
        loadUserData();
        loadRecentActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh activity on resume
        loadRecentActivity();
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnNotification = findViewById(R.id.btnNotification);
        recyclerActivity = findViewById(R.id.recyclerActivity);
        progressActivity = findViewById(R.id.progressActivity);
        tvNoActivity = findViewById(R.id.tvNoActivity);
    }

    private void setupRecyclerView() {
        activityAdapter = new ActivityAdapter();
        recyclerActivity.setLayoutManager(new LinearLayoutManager(this));
        recyclerActivity.setNestedScrollingEnabled(false);
        recyclerActivity.setAdapter(activityAdapter);
    }

    private void setupListeners() {
        // Notification Bell
        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        // Bottom Navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.navigation_orders) {
                startActivity(new Intent(this, OrdersActivity.class));
                return true;
            } else if (itemId == R.id.navigation_invoices) {
                startActivity(new Intent(this, InvoicesActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, AdminProfileActivity.class));
                return true;
            }
            return false;
        });

        // Quick Action Cards
        findViewById(R.id.cardQRCode).setOnClickListener(v -> {
            startActivity(new Intent(this, QRScannerActivity.class));
        });

        findViewById(R.id.cardManageBooks).setOnClickListener(v -> {
            startActivity(new Intent(this, ManageBooksActivity.class));
        });

        findViewById(R.id.cardReservations).setOnClickListener(v -> {
            startActivity(new Intent(this, ReservationManagementActivity.class));
        });

        findViewById(R.id.cardManageUsers).setOnClickListener(v -> {
            startActivity(new Intent(this, UserManagementActivity.class));
        });

        findViewById(R.id.cardViewRatings).setOnClickListener(v -> {
            startActivity(new Intent(this, ViewRatingsActivity.class));
        });
    }

    private void loadRecentActivity() {
        progressActivity.setVisibility(View.VISIBLE);
        tvNoActivity.setVisibility(View.GONE);
        recyclerActivity.setVisibility(View.GONE);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_GET_RECENT_ACTIVITY,
                null,
                response -> {
                    progressActivity.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray activities = response.getJSONArray("activities");
                            if (activities.length() > 0) {
                                activityAdapter.setActivities(activities);
                                recyclerActivity.setVisibility(View.VISIBLE);
                                tvNoActivity.setVisibility(View.GONE);
                            } else {
                                recyclerActivity.setVisibility(View.GONE);
                                tvNoActivity.setVisibility(View.VISIBLE);
                            }
                        } else {
                            tvNoActivity.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tvNoActivity.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    progressActivity.setVisibility(View.GONE);
                    tvNoActivity.setVisibility(View.VISIBLE);
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String userName = prefs.getString("userName", "Admin");
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void showAddBookDialog() {
        AddBookDialogFragment dialog = new AddBookDialogFragment();
        dialog.setOnBookAddedListener(() -> {
            Toast.makeText(this, "Book added to catalog!", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getSupportFragmentManager(), "AddBookDialog");
    }
}
