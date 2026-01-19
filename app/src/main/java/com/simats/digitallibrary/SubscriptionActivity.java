package com.simats.digitallibrary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SubscriptionActivity extends AppCompatActivity {

    private TextView btnUpgradePremium;
    private TextView btnSkipForNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnUpgradePremium = findViewById(R.id.btnUpgradePremium);
        btnSkipForNow = findViewById(R.id.btnSkipForNow);
    }

    private void setupClickListeners() {
        btnUpgradePremium.setOnClickListener(v -> {
            // Navigate to Login/Account Selection (same as skip)
            navigateToLogin();
        });

        btnSkipForNow.setOnClickListener(v -> {
            // Navigate to Login/Account Selection
            navigateToLogin();
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Go back to onboarding if pressed back
        finish();
    }
}
