package com.simats.digitallibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Register Activity
 * Handles user registration for both Reader and Admin
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // Views
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private EditText etLibraryName, etLibraryLocation;
    private LinearLayout layoutName, layoutLibraryName, layoutLibraryLocation;
    private TextView btnRegister;
    private ProgressBar progressBar;
    private ImageView ivPasswordToggle;
    private TextView tvLogin, tvTitle, tvSubtitle;

    private boolean passwordVisible = false;
    private String accountType = "reader";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Get account type from intent
        accountType = getIntent().getStringExtra(SelectAccountTypeActivity.EXTRA_ACCOUNT_TYPE);
        if (accountType == null)
            accountType = "reader";

        initViews();
        setupUI();
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etLibraryName = findViewById(R.id.etLibraryName);
        etLibraryLocation = findViewById(R.id.etLibraryLocation);

        layoutName = findViewById(R.id.layoutName);
        layoutLibraryName = findViewById(R.id.layoutLibraryName);
        layoutLibraryLocation = findViewById(R.id.layoutLibraryLocation);

        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle);
        tvLogin = findViewById(R.id.tvLogin);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
    }

    private void setupUI() {
        if ("admin".equals(accountType)) {
            // Admin: Show library fields, hide name
            tvTitle.setText("Admin Registration");
            tvSubtitle.setText("Register your library");
            layoutName.setVisibility(View.GONE);
            layoutLibraryName.setVisibility(View.VISIBLE);
            layoutLibraryLocation.setVisibility(View.VISIBLE);
        } else {
            // Reader: Show name, hide library fields
            tvTitle.setText("Create Account");
            tvSubtitle.setText("Join our library community");
            layoutName.setVisibility(View.VISIBLE);
            layoutLibraryName.setVisibility(View.GONE);
            layoutLibraryLocation.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> register());

        ivPasswordToggle.setOnClickListener(v -> togglePassword());

        tvLogin.setOnClickListener(v -> {
            // Only clear login state, preserve saved credentials
            getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                    .putBoolean("isLoggedIn", false)
                    .apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(SelectAccountTypeActivity.EXTRA_ACCOUNT_TYPE, accountType);
            startActivity(intent);
            finish();
        });
    }

    private void togglePassword() {
        if (passwordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivPasswordToggle.setImageResource(R.drawable.ic_visibility_off);
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivPasswordToggle.setImageResource(R.drawable.ic_visibility);
        }
        passwordVisible = !passwordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private void register() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Common validations
        if (email.isEmpty()) {
            etEmail.setError("Email required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Minimum 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords don't match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Type-specific validations
        String name = "";
        String libraryName = "";
        String libraryLocation = "";

        if ("admin".equals(accountType)) {
            libraryName = etLibraryName.getText().toString().trim();
            libraryLocation = etLibraryLocation.getText().toString().trim();

            if (libraryName.isEmpty()) {
                etLibraryName.setError("Library name required");
                etLibraryName.requestFocus();
                return;
            }
            if (libraryLocation.isEmpty()) {
                etLibraryLocation.setError("Location required");
                etLibraryLocation.requestFocus();
                return;
            }
            name = libraryName; // Use library name as name
        } else {
            name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Name required");
                etName.requestFocus();
                return;
            }
        }

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // Prepare JSON
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            jsonBody.put("role", accountType);

            if ("admin".equals(accountType)) {
                jsonBody.put("library_name", libraryName);
                jsonBody.put("library_location", libraryLocation);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        Log.d(TAG, "Register URL: " + ApiConfig.URL_REGISTER);
        Log.d(TAG, "Request: " + jsonBody.toString());

        // Make request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_REGISTER,
                jsonBody,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Log.d(TAG, "Response: " + response.toString());
                    handleResponse(response);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);

                    Log.e(TAG, "Register Error: " + error.getMessage());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Error Data: " + responseBody);

                            // Parse error message and show to user
                            JSONObject errorJson = new JSONObject(responseBody);
                            String errorMessage = errorJson.optString("message", "Registration failed");
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Registration failed", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "Network Response is null");
                        Toast.makeText(this, "Connection failed. Please check your network.", Toast.LENGTH_LONG).show();
                    }
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void handleResponse(JSONObject response) {
        try {
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                // Get user data from response
                JSONObject user = response.optJSONObject("user");
                int userId = user != null ? user.optInt("id", 0) : 0;
                String userName = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Save credentials to in-app password manager WITH role
                CredentialManager credentialManager = new CredentialManager(this);
                credentialManager.saveCredential(email, password, accountType);

                // Save session - auto login after registration
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                prefs.edit()
                        .putBoolean("isLoggedIn", true)
                        .putInt("userId", userId)
                        .putInt("user_id", userId)
                        .putString("userName", userName)
                        .putString("name", userName)
                        .putString("userEmail", email)
                        .putString("email", email)
                        .putString("userRole", accountType)
                        .putString("libraryName",
                                "admin".equalsIgnoreCase(accountType) ? etLibraryName.getText().toString().trim() : "")
                        .putString("libraryLocation",
                                "admin".equalsIgnoreCase(accountType) ? etLibraryLocation.getText().toString().trim()
                                        : "")
                        .apply();

                Toast.makeText(this, "Welcome, " + userName + "!", Toast.LENGTH_SHORT).show();

                // Navigate to dashboard based on role
                Intent intent;
                if ("admin".equalsIgnoreCase(accountType)) {
                    intent = new Intent(this, AdminDashboardActivity.class);
                } else {
                    intent = new Intent(this, MainActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
}
