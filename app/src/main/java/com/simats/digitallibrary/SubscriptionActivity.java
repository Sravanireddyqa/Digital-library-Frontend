package com.simats.digitallibrary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SubscriptionActivity extends AppCompatActivity {

    private TextView btnUpgradePremium;
    private TextView btnSkipForNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnUpgradePremium = findViewById(R.id.btnUpgradePremium);
        btnSkipForNow = findViewById(R.id.btnSkipForNow);
    }

    private void setupClickListeners() {
        // Upgrade to Premium - Show subscription not available message
        btnUpgradePremium.setOnClickListener(v -> {
            Toast.makeText(this, "Subscription not available. Please try again later.", Toast.LENGTH_LONG).show();
        });

        // Skip for now - Go to Reader/Admin selection page
        btnSkipForNow.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectAccountTypeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
