package com.simats.digitallibrary;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
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
 * Reservation Management Activity
 * Displays and manages book reservations
 */
public class ReservationManagementActivity extends AppCompatActivity {

    private static final String TAG = "ReservationMgmt";

    private ImageButton btnBack;
    private TextView tvPendingCount, tvApprovedCount, tvRejectedCount;
    private TextView tabAll, tabPending, tabApproved, tabRejected;
    private RecyclerView recyclerReservations;
    private ProgressBar progressBar;
    private View layoutEmpty;

    private ReservationAdapter adapter;
    private List<Reservation> allReservations = new ArrayList<>();
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_management);

        initViews();
        setupRecyclerView();
        setupTabs();
        loadReservations();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvApprovedCount = findViewById(R.id.tvApprovedCount);
        tvRejectedCount = findViewById(R.id.tvRejectedCount);
        tabAll = findViewById(R.id.tabAll);
        tabPending = findViewById(R.id.tabPending);
        tabApproved = findViewById(R.id.tabApproved);
        tabRejected = findViewById(R.id.tabRejected);
        recyclerReservations = findViewById(R.id.recyclerReservations);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ReservationAdapter();
        adapter.setOnReservationActionListener(new ReservationAdapter.OnReservationActionListener() {
            @Override
            public void onApprove(Reservation reservation) {
                updateReservationStatus(reservation, "approved");
            }

            @Override
            public void onReject(Reservation reservation) {
                updateReservationStatus(reservation, "rejected");
            }

            @Override
            public void onReturn(Reservation reservation) {
                showReturnDialog(reservation);
            }

            @Override
            public void onSendReminder(Reservation reservation) {
                sendReminderToUser(reservation);
            }
        });

        recyclerReservations.setLayoutManager(new LinearLayoutManager(this));
        recyclerReservations.setAdapter(adapter);
    }

    private void setupTabs() {
        View.OnClickListener tabListener = v -> {
            resetTabs();
            v.setBackgroundResource(R.drawable.bg_tab_selected);
            ((TextView) v).setTextColor(getColor(android.R.color.white));

            if (v == tabAll) {
                currentFilter = "all";
            } else if (v == tabPending) {
                currentFilter = "pending";
            } else if (v == tabApproved) {
                currentFilter = "approved";
            } else if (v == tabRejected) {
                currentFilter = "rejected";
            }
            filterReservations();
        };

        tabAll.setOnClickListener(tabListener);
        tabPending.setOnClickListener(tabListener);
        tabApproved.setOnClickListener(tabListener);
        tabRejected.setOnClickListener(tabListener);
    }

    private void resetTabs() {
        tabAll.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabAll.setTextColor(0xFF6B7280);
        tabPending.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabPending.setTextColor(0xFF6B7280);
        tabApproved.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabApproved.setTextColor(0xFF6B7280);
        tabRejected.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabRejected.setTextColor(0xFF6B7280);
    }

    private void loadReservations() {
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
                            JSONArray reservationsArray = response.getJSONArray("reservations");
                            allReservations.clear();

                            int pending = 0, approved = 0, rejected = 0;

                            for (int i = 0; i < reservationsArray.length(); i++) {
                                JSONObject obj = reservationsArray.getJSONObject(i);
                                Reservation r = new Reservation();
                                r.setId(obj.getInt("id"));
                                r.setReservationId(obj.optString("reservation_id", "RES-" + obj.getInt("id")));
                                r.setBookId(obj.optInt("book_id", 0));
                                r.setBookTitle(obj.optString("book_title", "Unknown Book"));
                                r.setUserId(obj.optInt("user_id", 0));
                                r.setUserName(obj.optString("user_name", "Unknown User"));
                                r.setUserEmail(obj.optString("user_email", ""));
                                r.setLibrary(obj.optString("library", "Library"));
                                r.setPickupDate(obj.optString("pickup_date", ""));
                                r.setPickupTime(obj.optString("pickup_time", ""));
                                r.setStatus(obj.optString("status", "pending"));
                                r.setRequestedAt(obj.optString("requested_at", ""));
                                r.setCancelReason(obj.optString("cancel_reason", ""));
                                allReservations.add(r);

                                // Count by status
                                String status = r.getStatus().toLowerCase();
                                if (status.equals("pending"))
                                    pending++;
                                else if (status.equals("approved"))
                                    approved++;
                                else if (status.equals("rejected"))
                                    rejected++;
                            }

                            // Update stats
                            tvPendingCount.setText(String.valueOf(pending));
                            tvApprovedCount.setText(String.valueOf(approved));
                            tvRejectedCount.setText(String.valueOf(rejected));

                            filterReservations();
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
                    Toast.makeText(this, "Failed to load reservations", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void filterReservations() {
        List<Reservation> filtered = new ArrayList<>();

        for (Reservation r : allReservations) {
            if (currentFilter.equals("all") ||
                    r.getStatus().toLowerCase().equals(currentFilter)) {
                filtered.add(r);
            }
        }

        adapter.setReservations(filtered);

        if (filtered.isEmpty()) {
            showEmpty();
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerReservations.setVisibility(View.VISIBLE);
        }
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerReservations.setVisibility(View.GONE);
    }

    private void updateReservationStatus(Reservation reservation, String newStatus) {
        JSONObject data = new JSONObject();
        try {
            data.put("reservation_id", reservation.getId());
            data.put("status", newStatus);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_UPDATE_RESERVATION,
                data,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this,
                                    "Reservation " + newStatus + "!",
                                    Toast.LENGTH_SHORT).show();
                            loadReservations(); // Refresh
                        } else {
                            Toast.makeText(this,
                                    response.optString("message", "Update failed"),
                                    Toast.LENGTH_SHORT).show();
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

    private void showReturnDialog(Reservation reservation) {
        // Get deposit amount (use book price as deposit for now)
        double depositAmount = 299; // Default, will be updated from API
        double bookPrice = 299;

        ReturnBookDialog dialog = new ReturnBookDialog(this, reservation, depositAmount, bookPrice);
        dialog.setOnReturnConfirmedListener((res, condition, daysLate, damageLevel, notes) -> {
            processReturn(res, condition, daysLate, damageLevel, notes);
        });
        dialog.show();
    }

    private void processReturn(Reservation reservation, String condition, int daysLate, String damageLevel,
            String notes) {
        JSONObject data = new JSONObject();
        try {
            data.put("reservation_id", reservation.getId());
            data.put("condition", condition);
            data.put("days_late", daysLate);
            data.put("damage_level", damageLevel);
            data.put("notes", notes);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_PROCESS_RETURN,
                data,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            double refund = response.optDouble("refund", 0);
                            double fine = response.optDouble("fine", 0);
                            String message = String.format("Return processed! Fine: ₹%.0f, Refund: ₹%.0f", fine,
                                    refund);
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            loadReservations(); // Refresh
                        } else {
                            Toast.makeText(this,
                                    response.optString("message", "Return failed"),
                                    Toast.LENGTH_SHORT).show();
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

    private void sendReminderToUser(Reservation reservation) {
        JSONObject data = new JSONObject();
        try {
            data.put("reservation_id", reservation.getId());
            data.put("type", "general");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_SEND_REMINDER,
                data,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            String bookTitle = response.optString("book_title", reservation.getBookTitle());
                            Toast.makeText(this, "📢 Reminder sent for '" + bookTitle + "'", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this,
                                    response.optString("message", "Failed to send reminder"),
                                    Toast.LENGTH_SHORT).show();
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
