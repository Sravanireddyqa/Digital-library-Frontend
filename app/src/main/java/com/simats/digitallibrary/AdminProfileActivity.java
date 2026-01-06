package com.simats.digitallibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Admin Profile Activity
 * Displays admin account information with edit and logout options
 */
public class AdminProfileActivity extends AppCompatActivity {

    private static final String TAG = "AdminProfileActivity";
    private static final String PREF_NAME = "UserSession";

    private TextView tvAvatarInitial, tvAdminName, tvEmail;
    private TextView tvLibraryName, tvLibraryLocation, tvMemberSince;
    private View btnLogout, btnEditProfile, btnChangePassword;
    private ImageButton btnBack;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        initViews();
        loadProfileData();
        setupListeners();
    }

    private void initViews() {
        tvAvatarInitial = findViewById(R.id.tvAvatarInitial);
        tvAdminName = findViewById(R.id.tvAdminName);
        tvEmail = findViewById(R.id.tvEmail);
        tvLibraryName = findViewById(R.id.tvLibraryName);
        tvLibraryLocation = findViewById(R.id.tvLibraryLocation);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadProfileData() {
        String name = prefs.getString("userName", "Admin");
        String email = prefs.getString("userEmail", "admin@email.com");
        String libraryName = prefs.getString("libraryName", "Digital Library");
        String libraryLocation = prefs.getString("libraryLocation", "Location not set");
        String createdAt = prefs.getString("createdAt", "");

        if (!name.isEmpty()) {
            tvAvatarInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

        tvAdminName.setText(name);
        tvEmail.setText(email);
        tvLibraryName.setText(libraryName);
        tvLibraryLocation.setText(libraryLocation);

        if (!createdAt.isEmpty()) {
            tvMemberSince.setText(formatDate(createdAt));
        } else {
            tvMemberSince.setText("Member");
        }

        fetchProfileFromServer();
    }

    private void fetchProfileFromServer() {
        int userId = prefs.getInt("userId", -1);
        if (userId == -1)
            return;

        String url = ApiConfig.URL_GET_PROFILE + "?user_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject user = response.getJSONObject("user");
                            updateUI(user);
                            cacheUserData(user);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching profile: " + error.getMessage());
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void updateUI(JSONObject user) throws JSONException {
        String name = user.optString("name", prefs.getString("userName", "Admin"));
        String email = user.optString("email", "");
        String libraryName = user.optString("library_name", "Digital Library");
        String libraryLocation = user.optString("library_location", "");
        String createdAt = user.optString("created_at", "");

        if (!name.isEmpty()) {
            tvAvatarInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
            tvAdminName.setText(name);
        }

        if (!email.isEmpty()) {
            tvEmail.setText(email);
        }

        tvLibraryName.setText(libraryName);
        tvLibraryLocation.setText(libraryLocation.isEmpty() ? "Location not set" : libraryLocation);

        if (!createdAt.isEmpty()) {
            tvMemberSince.setText(formatDate(createdAt));
        }
    }

    private void cacheUserData(JSONObject user) throws JSONException {
        prefs.edit()
                .putString("userName", user.optString("name", ""))
                .putString("userEmail", user.optString("email", ""))
                .putString("libraryName", user.optString("library_name", ""))
                .putString("libraryLocation", user.optString("library_location", ""))
                .putString("createdAt", user.optString("created_at", ""))
                .apply();
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return date != null ? outputFormat.format(date) : "Member";
        } catch (Exception e) {
            return "Member";
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Name input
        final EditText etName = new EditText(this);
        etName.setHint("Name");
        etName.setText(prefs.getString("userName", ""));
        layout.addView(etName);

        // Library Name input
        final EditText etLibraryName = new EditText(this);
        etLibraryName.setHint("Library Name");
        etLibraryName.setText(prefs.getString("libraryName", ""));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 20;
        etLibraryName.setLayoutParams(params);
        layout.addView(etLibraryName);

        // Library Location input
        final EditText etLocation = new EditText(this);
        etLocation.setHint("Library Location");
        etLocation.setText(prefs.getString("libraryLocation", ""));
        etLocation.setLayoutParams(params);
        layout.addView(etLocation);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String libraryName = etLibraryName.getText().toString().trim();
            String location = etLocation.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            updateProfile(name, libraryName, location);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateProfile(String name, String libraryName, String location) {
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("user_id", userId);
            requestBody.put("name", name);
            requestBody.put("library_name", libraryName);
            requestBody.put("library_location", location);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_UPDATE_PROFILE,
                    requestBody,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                                // Update local cache
                                prefs.edit()
                                        .putString("userName", name)
                                        .putString("libraryName", libraryName)
                                        .putString("libraryLocation", location)
                                        .apply();

                                // Reload UI
                                loadProfileData();
                            } else {
                                Toast.makeText(this, response.optString("message", "Update failed"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    });

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Current password
        final EditText etCurrentPassword = new EditText(this);
        etCurrentPassword.setHint("Current Password");
        etCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etCurrentPassword);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 20;

        // New password
        final EditText etNewPassword = new EditText(this);
        etNewPassword.setHint("New Password");
        etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etNewPassword.setLayoutParams(params);
        layout.addView(etNewPassword);

        // Confirm password
        final EditText etConfirmPassword = new EditText(this);
        etConfirmPassword.setHint("Confirm New Password");
        etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etConfirmPassword.setLayoutParams(params);
        layout.addView(etConfirmPassword);

        builder.setView(layout);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(currentPassword, newPassword);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void changePassword(String currentPassword, String newPassword) {
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("user_id", userId);
            requestBody.put("current_password", currentPassword);
            requestBody.put("new_password", newPassword);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_CHANGE_PASSWORD,
                    requestBody,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, response.optString("message", "Failed to change password"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(this, "Error changing password", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    });

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        prefs.edit().clear().apply();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
