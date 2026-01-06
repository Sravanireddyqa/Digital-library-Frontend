package com.simats.digitallibrary;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Reset Password Activity
 * Step 3: User sets new password after OTP verification
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;
    private TextView btnResetPassword;
    private ImageButton btnBack;
    private ImageView ivToggleNewPassword, ivToggleConfirmPassword;
    private ProgressBar progressBar;

    private String email, resetToken;
    private boolean newPasswordVisible = false;
    private boolean confirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        email = getIntent().getStringExtra("email");
        resetToken = getIntent().getStringExtra("reset_token");

        if (email == null || resetToken == null) {
            Toast.makeText(this, "Invalid session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.btnBack);
        ivToggleNewPassword = findViewById(R.id.ivToggleNewPassword);
        ivToggleConfirmPassword = findViewById(R.id.ivToggleConfirmPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnResetPassword.setOnClickListener(v -> resetPassword());

        ivToggleNewPassword.setOnClickListener(v -> {
            newPasswordVisible = !newPasswordVisible;
            togglePasswordVisibility(etNewPassword, ivToggleNewPassword, newPasswordVisible);
        });

        ivToggleConfirmPassword.setOnClickListener(v -> {
            confirmPasswordVisible = !confirmPasswordVisible;
            togglePasswordVisibility(etConfirmPassword, ivToggleConfirmPassword, confirmPasswordVisible);
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView icon, boolean visible) {
        if (visible) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            icon.setImageResource(R.drawable.ic_visibility);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            icon.setImageResource(R.drawable.ic_visibility_off);
        }
        editText.setSelection(editText.getText().length());
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            etNewPassword.setError("Password is required");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        showLoading(true);

        try {
            JSONObject request = new JSONObject();
            request.put("email", email);
            request.put("reset_token", resetToken);
            request.put("new_password", newPassword);

            JsonObjectRequest jsonRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_RESET_PASSWORD,
                    request,
                    response -> {
                        showLoading(false);
                        handleResponse(response);
                    },
                    error -> {
                        showLoading(false);
                        if (error.networkResponse != null) {
                            try {
                                String responseBody = new String(error.networkResponse.data, "UTF-8");
                                JSONObject errorJson = new JSONObject(responseBody);
                                Toast.makeText(this, errorJson.optString("message", "Reset failed"), Toast.LENGTH_LONG)
                                        .show();
                            } catch (Exception e) {
                                Toast.makeText(this, "Reset failed", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });

            VolleySingleton.getInstance(this).addToRequestQueue(jsonRequest);

        } catch (JSONException e) {
            showLoading(false);
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleResponse(JSONObject response) {
        try {
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            if (success) {
                // Navigate to login
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnResetPassword.setEnabled(!show);
        btnResetPassword.setAlpha(show ? 0.5f : 1f);
    }
}
