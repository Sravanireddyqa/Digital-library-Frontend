package com.simats.digitallibrary;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Login Activity
 * Handles user authentication with saved password support
 */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private static final String TAG = "LoginActivity";
    private static final String PREF_NAME = "UserSession";

    private EditText etEmail, etPassword;
    private TextView btnLogin;
    private ProgressBar progressBar;
    private ImageView ivPasswordToggle;
    private TextView tvSignUp, tvForgotPassword;
    private boolean passwordVisible = false;

    private CredentialManager credentialManager;
    private String pendingEmail, pendingPassword;
    private boolean savedAccountsShown = false; // Track if saved accounts dialog was shown
    private String accountType = "user"; // Default to user, can be "admin"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if already logged in
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        if (prefs.getBoolean("isLoggedIn", false)) {
            String role = prefs.getString("userRole", "reader");
            Intent intent;
            if ("admin".equalsIgnoreCase(role)) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else {
                intent = new Intent(this, MainActivity.class);
            }
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        credentialManager = new CredentialManager(this);

        // Get account type from intent (passed from RegisterActivity)
        String type = getIntent().getStringExtra(SelectAccountTypeActivity.EXTRA_ACCOUNT_TYPE);
        if (type != null) {
            accountType = type;
        }

        initViews();
        setupListeners();

        // Clear focus to prevent autofill popup (saved accounts shown only when user
        // clicks email)
        getWindow().getDecorView().clearFocus();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());

        ivPasswordToggle.setOnClickListener(v -> togglePassword());

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectAccountTypeActivity.class);
            intent.putExtra(SelectAccountTypeActivity.EXTRA_FLOW_TYPE, SelectAccountTypeActivity.FLOW_SIGNUP);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        // Show saved accounts only on FIRST focus when email is empty
        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && etEmail.getText().toString().isEmpty()
                    && (credentialManager.hasSavedAccounts("user") || credentialManager.hasSavedAccounts("admin"))
                    && !savedAccountsShown) {
                savedAccountsShown = true;
                showSavedAccountsDialog();
            }
        });

        // Don't show dialog on click - only on first focus
        // This allows keyboard to show directly on subsequent clicks
    }

    private void showSavedAccountsDialog() {
        // Get accounts based on account type, or get all if opened directly
        List<CredentialManager.SavedAccount> accounts;
        if ("admin".equalsIgnoreCase(accountType)) {
            // Admin login - show only admin accounts
            accounts = credentialManager.getSavedAccounts("admin");
        } else if ("reader".equalsIgnoreCase(accountType)) {
            // Reader login - show only reader/user accounts
            accounts = credentialManager.getSavedAccounts("user");
        } else {
            // Default/unknown - show all accounts (both reader and admin)
            accounts = new ArrayList<>();
            accounts.addAll(credentialManager.getSavedAccounts("user"));
            accounts.addAll(credentialManager.getSavedAccounts("admin"));
        }

        if (accounts.isEmpty())
            return;

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_saved_accounts);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Position at bottom - no animation
        dialog.getWindow().setGravity(android.view.Gravity.BOTTOM);

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerAccounts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SavedAccountsAdapter adapter = new SavedAccountsAdapter(accounts, account -> {
            // Fill in the credentials only - user must click Login to proceed
            Log.d(TAG, "Selected account - Email: " + account.email + ", Password length: "
                    + (account.password != null ? account.password.length() : 0));
            etEmail.setText(account.email);
            etPassword.setText(account.password);
            dialog.dismiss();
        });
        recyclerView.setAdapter(adapter);

        dialog.findViewById(R.id.btnNoThanks).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void togglePassword() {
        if (passwordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivPasswordToggle.setImageResource(R.drawable.ic_visibility_off);
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivPasswordToggle.setImageResource(R.drawable.ic_visibility);
        }
        passwordVisible = !passwordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            etEmail.setError("Email required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Store for saving later
        pendingEmail = email;
        pendingPassword = password;

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Prepare JSON request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        Log.d(TAG, "Login URL: " + ApiConfig.URL_LOGIN);
        Log.d(TAG, "Request: " + jsonBody.toString());

        // Make request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_LOGIN,
                jsonBody,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Log.d(TAG, "Response: " + response.toString());
                    handleLoginResponse(response);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    // Detailed error logging
                    Log.e(TAG, "Login Error: " + error.getMessage());
                    Log.e(TAG, "Error Class: " + error.getClass().getSimpleName());

                    String errorMsg = "Connection failed";
                    if (error instanceof com.android.volley.TimeoutError) {
                        Log.e(TAG, "TIMEOUT ERROR - Server took too long to respond");
                        errorMsg = "Server timeout. Is XAMPP running?";
                    } else if (error instanceof com.android.volley.NoConnectionError) {
                        Log.e(TAG, "NO CONNECTION ERROR - Cannot reach server");
                        errorMsg = "Cannot connect to server. Check WiFi and IP.";
                    } else if (error instanceof com.android.volley.NetworkError) {
                        Log.e(TAG, "NETWORK ERROR - General network issue");
                        errorMsg = "Network error. Check your connection.";
                    } else if (error instanceof com.android.volley.ServerError) {
                        Log.e(TAG, "SERVER ERROR - Server returned error");
                        errorMsg = "Server error. Check PHP logs.";
                    } else if (error instanceof com.android.volley.ParseError) {
                        Log.e(TAG, "PARSE ERROR - Invalid JSON response");
                        errorMsg = "Invalid server response.";
                    } else if (error instanceof com.android.volley.AuthFailureError) {
                        Log.e(TAG, "AUTH ERROR - Authentication failed");
                        errorMsg = "Authentication error.";
                    }

                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Error Data: " + responseBody);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(TAG, "Network Response is null - likely timeout or no connection");
                    }

                    if (error.getCause() != null) {
                        Log.e(TAG, "Cause: " + error.getCause().toString());
                    }

                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void handleLoginResponse(JSONObject response) {
        try {
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                JSONObject user = response.getJSONObject("user");
                String role = user.getString("role");

                // Save credentials to in-app password manager WITH role
                Log.d(TAG, "Saving credential - Email: " + pendingEmail + ", Password length: "
                        + (pendingPassword != null ? pendingPassword.length() : 0) + ", Role: " + role);
                credentialManager.saveCredential(pendingEmail, pendingPassword, role);

                // Save session
                SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                prefs.edit()
                        .putBoolean("isLoggedIn", true)
                        .putInt("userId", user.getInt("id"))
                        .putInt("user_id", user.getInt("id"))
                        .putString("userName", user.getString("name"))
                        .putString("name", user.getString("name"))
                        .putString("userEmail", user.getString("email"))
                        .putString("email", user.getString("email"))
                        .putString("userRole", role)
                        .putString("libraryName", user.optString("library_name", ""))
                        .putString("libraryLocation", user.optString("library_location", ""))
                        .putString("createdAt", user.optString("created_at", ""))
                        .apply();

                // Request notification permission for Android 13+
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (checkSelfPermission(
                            android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[] { android.Manifest.permission.POST_NOTIFICATIONS }, 100);
                    }
                }

                // Register FCM token for push notifications
                try {
                    int userId = user.getInt("id");
                    FirebaseTokenManager tokenManager = new FirebaseTokenManager(this);
                    tokenManager.registerToken(userId);
                    Log.d(TAG, "FCM token registration initiated for user ID: " + userId);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to register FCM token: " + e.getMessage());
                }

                Toast.makeText(this, "Welcome, " + user.getString("name"), Toast.LENGTH_SHORT).show();

                // Navigate based on role
                Intent intent;
                if ("admin".equalsIgnoreCase(role)) {
                    intent = new Intent(this, AdminDashboardActivity.class);
                } else {
                    intent = new Intent(this, ReaderDashboardActivity.class);
                }
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Parse error: " + e.getMessage());
            Toast.makeText(this, "Server error", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Adapter for saved accounts list
     */
    private static class SavedAccountsAdapter extends RecyclerView.Adapter<SavedAccountsAdapter.ViewHolder> {

        private final List<CredentialManager.SavedAccount> accounts;
        private final OnAccountClickListener listener;

        interface OnAccountClickListener {
            void onAccountClick(CredentialManager.SavedAccount account);
        }

        SavedAccountsAdapter(List<CredentialManager.SavedAccount> accounts, OnAccountClickListener listener) {
            this.accounts = accounts;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_saved_account, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CredentialManager.SavedAccount account = accounts.get(position);
            holder.tvEmail.setText(account.email);
            holder.tvPassword.setText(account.getMaskedPassword());
            holder.itemView.setOnClickListener(v -> listener.onAccountClick(account));
        }

        @Override
        public int getItemCount() {
            return accounts.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmail, tvPassword;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvPassword = itemView.findViewById(R.id.tvPassword);
            }
        }
    }
}
