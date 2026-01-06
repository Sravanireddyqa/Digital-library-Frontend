package com.simats.digitallibrary;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
 * User Management Activity
 * Displays and manages all users
 */
public class UserManagementActivity extends AppCompatActivity {

    private static final String TAG = "UserManagement";

    private ImageButton btnBack;
    private TextView tvTotalUsersSubtitle, tvTotalUsers, tvActiveUsers, tvBlockedUsers, tvAdmins;
    private EditText etSearch;
    private Spinner spinnerType, spinnerStatus;
    private RecyclerView recyclerUsers;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private View cardTotalUsers, cardActiveUsers, cardBlockedUsers, cardAdmins;

    private UserAdapter adapter;
    private List<User> allUsers = new ArrayList<>();
    private String filterType = "all";
    private String filterStatus = "all";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        initViews();
        setupRecyclerView();
        setupSpinners();
        setupSearch();
        loadUsers();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalUsersSubtitle = findViewById(R.id.tvTotalUsersSubtitle);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvActiveUsers = findViewById(R.id.tvActiveUsers);
        tvBlockedUsers = findViewById(R.id.tvBlockedUsers);
        tvAdmins = findViewById(R.id.tvAdmins);
        etSearch = findViewById(R.id.etSearch);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        recyclerUsers = findViewById(R.id.recyclerUsers);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        // Stat cards
        cardTotalUsers = findViewById(R.id.cardTotalUsers);
        cardActiveUsers = findViewById(R.id.cardActiveUsers);
        cardBlockedUsers = findViewById(R.id.cardBlockedUsers);
        cardAdmins = findViewById(R.id.cardAdmins);

        btnBack.setOnClickListener(v -> finish());

        // Card click listeners for filtering
        cardTotalUsers.setOnClickListener(v -> {
            filterType = "all";
            filterStatus = "all";
            spinnerType.setSelection(0);
            spinnerStatus.setSelection(0);
            filterUsers();
            Toast.makeText(this, "Showing all users", Toast.LENGTH_SHORT).show();
        });

        cardActiveUsers.setOnClickListener(v -> {
            filterType = "all";
            filterStatus = "active";
            spinnerType.setSelection(0);
            spinnerStatus.setSelection(1);
            filterUsers();
            Toast.makeText(this, "Showing active users", Toast.LENGTH_SHORT).show();
        });

        cardBlockedUsers.setOnClickListener(v -> {
            filterType = "all";
            filterStatus = "blocked";
            spinnerType.setSelection(0);
            spinnerStatus.setSelection(2);
            filterUsers();
            Toast.makeText(this, "Showing blocked users", Toast.LENGTH_SHORT).show();
        });

        cardAdmins.setOnClickListener(v -> {
            filterType = "admin";
            filterStatus = "all";
            spinnerType.setSelection(2);
            spinnerStatus.setSelection(0);
            filterUsers();
            Toast.makeText(this, "Showing admins", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter();
        adapter.setOnUserActionListener(new UserAdapter.OnUserActionListener() {
            @Override
            public void onBlockUser(User user) {
                updateUserStatus(user, "blocked");
            }

            @Override
            public void onUnblockUser(User user) {
                updateUserStatus(user, "active");
            }
        });

        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsers.setNestedScrollingEnabled(true);
        recyclerUsers.setAdapter(adapter);
    }

    private void setupSpinners() {
        // Type spinner
        String[] types = { "All Types", "User", "Admin" };
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(typeAdapter);
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterType = position == 0 ? "all" : types[position].toLowerCase();
                filterUsers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Status spinner
        String[] statuses = { "All Statuses", "Active", "Blocked" };
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterStatus = position == 0 ? "all" : statuses[position].toLowerCase();
                filterUsers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase().trim();
                filterUsers();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_GET_USERS,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray usersArray = response.getJSONArray("users");
                            allUsers.clear();

                            int total = 0, active = 0, blocked = 0, admins = 0;

                            for (int i = 0; i < usersArray.length(); i++) {
                                JSONObject obj = usersArray.getJSONObject(i);
                                User user = new User();
                                user.setId(obj.getInt("id"));
                                user.setName(obj.optString("name", "Unknown"));
                                user.setEmail(obj.optString("email", ""));
                                user.setType(obj.optString("type", "user"));
                                user.setStatus(obj.optString("status", "active"));
                                user.setJoinedDate(obj.optString("joined_date", ""));
                                user.setReservationsCount(obj.optInt("reservations_count", 0));
                                allUsers.add(user);

                                total++;
                                if ("active".equalsIgnoreCase(user.getStatus()))
                                    active++;
                                if ("blocked".equalsIgnoreCase(user.getStatus()))
                                    blocked++;
                                if ("admin".equalsIgnoreCase(user.getType()))
                                    admins++;
                            }

                            // Update stats
                            tvTotalUsersSubtitle.setText(total + " total users");
                            tvTotalUsers.setText(String.valueOf(total));
                            tvActiveUsers.setText(String.valueOf(active));
                            tvBlockedUsers.setText(String.valueOf(blocked));
                            tvAdmins.setText(String.valueOf(admins));

                            filterUsers();
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
                    Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void filterUsers() {
        List<User> filtered = new ArrayList<>();

        for (User user : allUsers) {
            // Type filter
            if (!filterType.equals("all") && !user.getType().equalsIgnoreCase(filterType)) {
                continue;
            }

            // Status filter
            if (!filterStatus.equals("all") && !user.getStatus().equalsIgnoreCase(filterStatus)) {
                continue;
            }

            // Search filter
            if (!searchQuery.isEmpty()) {
                String name = user.getName().toLowerCase();
                String email = user.getEmail().toLowerCase();
                if (!name.contains(searchQuery) && !email.contains(searchQuery)) {
                    continue;
                }
            }

            filtered.add(user);
        }

        adapter.setUsers(filtered);

        if (filtered.isEmpty()) {
            showEmpty();
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerUsers.setVisibility(View.VISIBLE);
        }
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerUsers.setVisibility(View.GONE);
    }

    private void updateUserStatus(User user, String newStatus) {
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", user.getId());
            data.put("status", newStatus);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_UPDATE_USER,
                data,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this,
                                    "User " + newStatus + "!",
                                    Toast.LENGTH_SHORT).show();
                            loadUsers(); // Refresh
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
}
