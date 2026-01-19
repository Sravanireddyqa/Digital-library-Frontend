package com.simats.digitallibrary;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
        // Always set Home as selected when returning to dashboard
        bottomNavigation.setSelectedItemId(R.id.navigation_home);
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

        findViewById(R.id.cardLibraryClosure).setOnClickListener(v -> {
            showLibraryClosureDialog();
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

    /**
     * Show dialog to mark library as closed for a specific date
     */
    private void showLibraryClosureDialog() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1); // Default to tomorrow

        // First, show date picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDate.getTime());
                    String displayDate = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(selectedDate.getTime());

                    // Now show reason dialog
                    showReasonDialog(dateStr, displayDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.setTitle("Select Closure Date");
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // No past dates
        datePickerDialog.show();
    }

    private void showReasonDialog(String dateStr, String displayDate) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_library_closure, null);
        EditText etReason = dialogView.findViewById(R.id.etClosureReason);
        TextView tvDate = dialogView.findViewById(R.id.tvClosureDate);
        tvDate.setText("Closure Date: " + displayDate);

        new AlertDialog.Builder(this)
                .setTitle("📢 Mark Library Closed")
                .setView(dialogView)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String reason = etReason.getText().toString().trim();
                    if (reason.isEmpty()) {
                        reason = "Library Closed";
                    }
                    submitLibraryClosure(dateStr, reason);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitLibraryClosure(String dateStr, String reason) {
        try {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            int adminId = prefs.getInt("userId", 0);

            JSONObject params = new JSONObject();
            params.put("closed_date", dateStr);
            params.put("reason", reason);
            params.put("admin_id", adminId);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.BASE_URL + "add_library_closure.php",
                    params,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(this, "✅ Library closure added!\nUsers have been notified.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Error: " + response.optString("message"), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        Toast.makeText(this, "Failed to add closure. Please try again.", Toast.LENGTH_SHORT).show();
                    });

            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
