package com.simats.digitallibrary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Forgot Password Activity
 * Step 1: User enters email to receive OTP
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private TextView btnSendOTP, tvBackToLogin;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        btnBack = findViewById(R.id.btnBack);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        tvBackToLogin.setOnClickListener(v -> finish());

        btnSendOTP.setOnClickListener(v -> sendOTP());
    }

    private void sendOTP() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        showLoading(true);

        try {
            JSONObject request = new JSONObject();
            request.put("email", email);

            JsonObjectRequest jsonRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_FORGOT_PASSWORD,
                    request,
                    response -> {
                        showLoading(false);
                        handleResponse(response, email);
                    },
                    error -> {
                        showLoading(false);
                        if (error.networkResponse != null) {
                            try {
                                String responseBody = new String(error.networkResponse.data, "UTF-8");
                                JSONObject errorJson = new JSONObject(responseBody);
                                Toast.makeText(this, errorJson.optString("message", "Error sending OTP"),
                                        Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(this, "Error sending OTP", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });

            VolleySingleton.getInstance(this).addToRequestQueue(jsonRequest);

        } catch (JSONException e) {
            showLoading(false);
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleResponse(JSONObject response, String email) {
        try {
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                Toast.makeText(this, "OTP sent to your email!", Toast.LENGTH_SHORT).show();

                // For development - show OTP if returned (remove in production)
                String devOtp = response.optString("otp", null);
                if (devOtp != null) {
                    Toast.makeText(this, "Dev OTP: " + devOtp, Toast.LENGTH_LONG).show();
                }

                // Navigate to OTP verification
                Intent intent = new Intent(this, VerifyOTPActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSendOTP.setEnabled(!show);
        btnSendOTP.setAlpha(show ? 0.5f : 1f);
    }
}
