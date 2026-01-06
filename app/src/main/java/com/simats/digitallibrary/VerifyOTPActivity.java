package com.simats.digitallibrary;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
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
 * Verify OTP Activity
 * Step 2: User enters the 6-digit OTP received in email
 */
public class VerifyOTPActivity extends AppCompatActivity {

    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private EditText[] otpFields;
    private TextView btnVerify, tvTimer, tvResendOTP, tvSubtitle;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private String email;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        email = getIntent().getStringExtra("email");
        if (email == null) {
            finish();
            return;
        }

        initViews();
        setupOTPFields();
        setupListeners();
        startTimer();
    }

    private void initViews() {
        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        etOtp5 = findViewById(R.id.etOtp5);
        etOtp6 = findViewById(R.id.etOtp6);
        otpFields = new EditText[] { etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6 };

        btnVerify = findViewById(R.id.btnVerify);
        btnBack = findViewById(R.id.btnBack);
        tvTimer = findViewById(R.id.tvTimer);
        tvResendOTP = findViewById(R.id.tvResendOTP);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        progressBar = findViewById(R.id.progressBar);

        tvSubtitle.setText("Enter the 6-digit code sent to\n" + maskEmail(email));
    }

    private void setupOTPFields() {
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    }
                    if (s.length() == 0 && index > 0) {
                        otpFields[index - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
        etOtp1.requestFocus();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnVerify.setOnClickListener(v -> verifyOTP());
        tvResendOTP.setOnClickListener(v -> resendOTP());
    }

    private void startTimer() {
        tvResendOTP.setVisibility(View.GONE);
        tvTimer.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Resend OTP in " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                tvTimer.setVisibility(View.GONE);
                tvResendOTP.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private String getOTP() {
        StringBuilder otp = new StringBuilder();
        for (EditText field : otpFields) {
            otp.append(field.getText().toString());
        }
        return otp.toString();
    }

    private void verifyOTP() {
        String otp = getOTP();

        if (otp.length() != 6) {
            Toast.makeText(this, "Please enter complete 6-digit OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        try {
            JSONObject request = new JSONObject();
            request.put("email", email);
            request.put("otp", otp);

            JsonObjectRequest jsonRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_VERIFY_OTP,
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
                                Toast.makeText(this, errorJson.optString("message", "Invalid OTP"), Toast.LENGTH_LONG)
                                        .show();
                            } catch (Exception e) {
                                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
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

            if (success) {
                String resetToken = response.getString("reset_token");
                Toast.makeText(this, "OTP Verified!", Toast.LENGTH_SHORT).show();

                // Navigate to reset password
                Intent intent = new Intent(this, ResetPasswordActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("reset_token", resetToken);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                clearOTPFields();
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
        }
    }

    private void resendOTP() {
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
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(this, "New OTP sent!", Toast.LENGTH_SHORT).show();
                                // For dev - show OTP
                                String devOtp = response.optString("otp", null);
                                if (devOtp != null) {
                                    Toast.makeText(this, "Dev OTP: " + devOtp, Toast.LENGTH_LONG).show();
                                }
                                clearOTPFields();
                                startTimer();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        showLoading(false);
                        Toast.makeText(this, "Error resending OTP", Toast.LENGTH_SHORT).show();
                    });

            VolleySingleton.getInstance(this).addToRequestQueue(jsonRequest);

        } catch (JSONException e) {
            showLoading(false);
        }
    }

    private void clearOTPFields() {
        for (EditText field : otpFields) {
            field.setText("");
        }
        etOtp1.requestFocus();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!show);
        btnVerify.setAlpha(show ? 0.5f : 1f);
    }

    private String maskEmail(String email) {
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        String masked = name.substring(0, Math.min(2, name.length())) + "***"
                + name.substring(Math.max(0, name.length() - 2));
        return masked + "@" + domain;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
