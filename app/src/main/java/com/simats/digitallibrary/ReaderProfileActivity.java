package com.simats.digitallibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Reader Profile Activity
 */
public class ReaderProfileActivity extends AppCompatActivity {

    private static final String PREF_NAME = "UserSession";

    private TextView tvInitial, tvName, tvEmail;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_profile);

        initViews();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        tvInitial = findViewById(R.id.tvInitial);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String name = prefs.getString("name", "Reader");
        String email = prefs.getString("email", "reader@example.com");

        tvName.setText(name);
        tvEmail.setText(email);
        tvInitial.setText(name.substring(0, 1).toUpperCase());
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btnMyBookings).setOnClickListener(v -> {
            startActivity(new Intent(this, MyBookingsActivity.class));
        });

        findViewById(R.id.btnWishlist).setOnClickListener(v -> {
            startActivity(new Intent(this, WishlistActivity.class));
        });

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            confirmLogout();
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (d, w) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
