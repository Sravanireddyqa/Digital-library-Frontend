package com.simats.digitallibrary;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationClickListener {

    private static final String TAG = "NotificationsActivity";

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private View emptyState;
    private FloatingActionButton fabMarkAllRead;

    private SessionManager sessionManager;
    private int userId;

    // Firebase Realtime Database
    private DatabaseReference notificationsRef;
    private ChildEventListener notificationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Initialize session
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }
        userId = sessionManager.getUserId();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewNotifications);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        fabMarkAllRead = findViewById(R.id.fabMarkAllRead);

        // Setup RecyclerView
        adapter = new NotificationAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Setup swipe refresh
        swipeRefresh.setOnRefreshListener(this::loadNotifications);

        // Mark all as read FAB
        fabMarkAllRead.setOnClickListener(v -> showMarkAllAsReadDialog());

        // Initialize Firebase
        setupFirebaseListener();

        // Load notifications
        loadNotifications();
    }

    /**
     * Setup Firebase Realtime Database listener for real-time updates
     */
    private void setupFirebaseListener() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        notificationsRef = database.getReference("notifications").child(String.valueOf(userId));

        notificationListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                handleFirebaseNotification(snapshot, true);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                handleFirebaseNotification(snapshot, false);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Handle notification removal if needed
                loadNotifications();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Not needed for notifications
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase listener cancelled: " + error.getMessage());
            }
        };

        notificationsRef.addChildEventListener(notificationListener);
    }

    /**
     * Handle real-time notification from Firebase
     */
    private void handleFirebaseNotification(DataSnapshot snapshot, boolean isNew) {
        try {
            String id = snapshot.getKey();
            String type = snapshot.child("type").getValue(String.class);
            String title = snapshot.child("title").getValue(String.class);
            String message = snapshot.child("message").getValue(String.class);
            Long timestamp = snapshot.child("timestamp").getValue(Long.class);
            Boolean isRead = snapshot.child("isRead").getValue(Boolean.class);

            if (type != null && title != null && message != null) {
                Notification notification = new Notification();
                notification.setId(id);
                notification.setUserId(userId);
                notification.setType(NotificationType.fromKey(type));
                notification.setTitle(title);
                notification.setMessage(message);
                notification.setTimestamp(timestamp != null ? timestamp : System.currentTimeMillis());
                notification.setRead(isRead != null ? isRead : false);

                if (isNew) {
                    // Refresh the full list to maintain order
                    loadNotifications();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Firebase notification", e);
        }
    }

    /**
     * Load notifications from backend
     */
    private void loadNotifications() {
        showLoading();

        String url = ApiConfig.URL_GET_NOTIFICATIONS + "?user_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    hideLoading();
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONArray notificationsArray = response.getJSONArray("notifications");
                            List<Notification> notifications = parseNotifications(notificationsArray);
                            adapter.setNotifications(notifications);
                            updateEmptyState();
                            updateFabVisibility();
                        } else {
                            String message = response.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            updateEmptyState();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        Toast.makeText(this, "Error loading notifications", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    hideLoading();
                    Log.e(TAG, "Error loading notifications: " + error.toString());
                    Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Parse notifications JSON array
     */
    private List<Notification> parseNotifications(JSONArray array) throws JSONException {
        List<Notification> notifications = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);

            Notification notification = new Notification();
            notification.setId(obj.getString("id"));
            notification.setUserId(obj.getInt("user_id"));
            notification.setType(NotificationType.fromKey(obj.getString("type")));
            notification.setTitle(obj.getString("title"));
            notification.setMessage(obj.getString("message"));
            notification.setRead(obj.getBoolean("is_read"));

            // Parse timestamp
            String createdAt = obj.getString("created_at");
            notification.setTimestamp(parseTimestamp(createdAt));

            // Parse data if exists
            if (obj.has("data") && !obj.isNull("data")) {
                notification.setData(obj.getString("data"));
            }

            notifications.add(notification);
        }

        return notifications;
    }

    /**
     * Parse timestamp string to milliseconds
     */
    private long parseTimestamp(String timestamp) {
        try {
            // Assuming MySQL DATETIME format: 2024-01-02 15:30:00
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date date = sdf.parse(timestamp);
            return date != null ? date.getTime() : System.currentTimeMillis();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    /**
     * Mark notification as read
     */
    private void markAsRead(Notification notification) {
        if (notification.isRead()) {
            return; // Already read
        }

        try {
            JSONObject params = new JSONObject();
            params.put("notification_id", notification.getId());
            params.put("user_id", userId);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_MARK_NOTIFICATION_READ,
                    params,
                    response -> {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                notification.setRead(true);
                                adapter.notifyDataSetChanged();
                                updateFabVisibility();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON error", e);
                        }
                    },
                    error -> Log.e(TAG, "Error marking notification as read", error));

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating request", e);
        }
    }

    /**
     * Mark all notifications as read
     */
    private void markAllAsRead() {
        try {
            JSONObject params = new JSONObject();
            params.put("user_id", userId);
            params.put("mark_all", true);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_MARK_NOTIFICATION_READ,
                    params,
                    response -> {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                adapter.markAllAsRead();
                                updateFabVisibility();
                                Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON error", e);
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error marking all as read", error);
                        Toast.makeText(this, "Failed to mark all as read", Toast.LENGTH_SHORT).show();
                    });

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating request", e);
        }
    }

    /**
     * Delete notification
     */
    private void deleteNotification(Notification notification, int position) {
        try {
            JSONObject params = new JSONObject();
            params.put("notification_id", notification.getId());
            params.put("user_id", userId);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_DELETE_NOTIFICATION,
                    params,
                    response -> {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                adapter.removeNotification(position);
                                updateEmptyState();
                                updateFabVisibility();
                                Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON error", e);
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error deleting notification", error);
                        Toast.makeText(this, "Failed to delete notification", Toast.LENGTH_SHORT).show();
                    });

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating request", e);
        }
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Mark as read
        markAsRead(notification);

        // Handle notification action based on type
        handleNotificationAction(notification);
    }

    @Override
    public void onNotificationLongClick(Notification notification) {
        int position = adapter.getNotifications().indexOf(notification);
        showDeleteDialog(notification, position);
    }

    /**
     * Handle notification click action
     */
    private void handleNotificationAction(Notification notification) {
        // You can add specific actions based on notification type
        // For example: open book details, open my bookings, etc.
        Toast.makeText(this, notification.getMessage(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Show mark all as read confirmation dialog
     */
    private void showMarkAllAsReadDialog() {
        if (adapter.getUnreadCount() == 0) {
            Toast.makeText(this, "All notifications are already read", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Mark All as Read")
                .setMessage("Mark all " + adapter.getUnreadCount() + " unread notifications as read?")
                .setPositiveButton("Yes", (dialog, which) -> markAllAsRead())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Show delete confirmation dialog
     */
    private void showDeleteDialog(Notification notification, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Notification")
                .setMessage("Are you sure you want to delete this notification?")
                .setPositiveButton("Delete", (dialog, which) -> deleteNotification(notification, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLoading() {
        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateFabVisibility() {
        if (adapter.getUnreadCount() > 0) {
            fabMarkAllRead.show();
        } else {
            fabMarkAllRead.hide();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove Firebase listener
        if (notificationsRef != null && notificationListener != null) {
            notificationsRef.removeEventListener(notificationListener);
        }
    }
}
