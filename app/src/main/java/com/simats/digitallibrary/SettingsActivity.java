package com.simats.digitallibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * Settings Activity
 * Comprehensive settings for user preferences, notifications, and app info
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final String PREF_NAME = "UserSession";
    private static final String SETTINGS_PREF = "AppSettings";

    // Notification switches
    private SwitchCompat switchNewBooks, switchReservation, switchDueDate;

    // Search preferences
    private SwitchCompat switchSemantic;
    private Spinner spinnerCategory, spinnerLanguage;

    // Privacy
    private SwitchCompat switchRememberMe;
    private Spinner spinnerLogoutTimer;

    private SharedPreferences prefs, settingsPrefs;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        settingsPrefs = getSharedPreferences(SETTINGS_PREF, MODE_PRIVATE);

        initViews();
        loadSettings();
        setupLanguageListener(); // Set listener after loading to prevent mismatch
        setupClickListeners();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Notification switches
        switchNewBooks = findViewById(R.id.switchNewBooks);
        switchReservation = findViewById(R.id.switchReservation);
        switchDueDate = findViewById(R.id.switchDueDate);

        // Search preferences
        switchSemantic = findViewById(R.id.switchSemantic);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);

        // Privacy
        switchRememberMe = findViewById(R.id.switchRememberMe);
        spinnerLogoutTimer = findViewById(R.id.spinnerLogoutTimer);

        // Setup spinners
        setupSpinners();
    }

    private void setupSpinners() {
        // Category spinner
        String[] categories = { "All Categories", "Fiction", "Non-Fiction", "Mythology", "Educational Books", "Science",
                "Technology" };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, categories);
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_dark);
        spinnerCategory.setAdapter(categoryAdapter);

        // Language spinner
        String[] languages = { "English", "తెలుగు (Telugu)", "हिंदी (Hindi)", "தமிழ் (Tamil)" };
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, languages);
        languageAdapter.setDropDownViewResource(R.layout.spinner_dropdown_dark);
        spinnerLanguage.setAdapter(languageAdapter);

        // Logout timer spinner
        String[] timers = { "5 minutes", "10 minutes", "15 minutes", "30 minutes", "Never" };
        ArrayAdapter<String> timerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, timers);
        timerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_dark);
        spinnerLogoutTimer.setAdapter(timerAdapter);

        // Note: Language listener is set after loadSettings() to prevent mismatch
    }

    private void setupLanguageListener() {
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isInitializing = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Skip the first automatic trigger when setting adapter
                if (isInitializing) {
                    isInitializing = false;
                    return;
                }

                // Get selected and current language
                String selectedLangCode = LocaleHelper.getLanguageCode(position);
                String currentLangCode = LocaleHelper.getLanguage(SettingsActivity.this);

                // Only change if different from saved preference
                if (!selectedLangCode.equals(currentLangCode)) {
                    LocaleHelper.applyLanguage(SettingsActivity.this, selectedLangCode);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadSettings() {
        // Notifications
        switchNewBooks.setChecked(settingsPrefs.getBoolean("notify_new_books", true));
        switchReservation.setChecked(settingsPrefs.getBoolean("notify_reservation", true));
        switchDueDate.setChecked(settingsPrefs.getBoolean("notify_due_date", true));

        // Search
        switchSemantic.setChecked(settingsPrefs.getBoolean("semantic_search", true));
        spinnerCategory.setSelection(settingsPrefs.getInt("preferred_category", 0));

        // Language - set spinner to match saved language
        String langCode = LocaleHelper.getLanguage(this);
        spinnerLanguage.setSelection(LocaleHelper.getLanguagePosition(langCode));

        // Privacy
        switchRememberMe.setChecked(settingsPrefs.getBoolean("remember_me", false));
        spinnerLogoutTimer.setSelection(settingsPrefs.getInt("logout_timer", 2));
    }

    private void setupClickListeners() {
        // Edit Profile
        findViewById(R.id.btnChangeName).setOnClickListener(v -> showChangeNameDialog());
        findViewById(R.id.btnChangePhone).setOnClickListener(v -> showChangePhoneDialog());

        // Change Password
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showChangePasswordDialog());

        // Notification switches
        switchNewBooks.setOnCheckedChangeListener(
                (b, checked) -> settingsPrefs.edit().putBoolean("notify_new_books", checked).apply());
        switchReservation.setOnCheckedChangeListener(
                (b, checked) -> settingsPrefs.edit().putBoolean("notify_reservation", checked).apply());
        switchDueDate.setOnCheckedChangeListener(
                (b, checked) -> settingsPrefs.edit().putBoolean("notify_due_date", checked).apply());

        // Search preferences
        switchSemantic.setOnCheckedChangeListener(
                (b, checked) -> settingsPrefs.edit().putBoolean("semantic_search", checked).apply());

        // Privacy
        switchRememberMe.setOnCheckedChangeListener(
                (b, checked) -> settingsPrefs.edit().putBoolean("remember_me", checked).apply());

        // Clear history
        findViewById(R.id.btnClearHistory).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_clear_history_title))
                    .setMessage(getString(R.string.dialog_clear_history_message))
                    .setPositiveButton(getString(R.string.btn_clear), (d, w) -> {
                        settingsPrefs.edit().remove("search_history").apply();
                        Toast.makeText(this, getString(R.string.msg_history_cleared), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(getString(R.string.btn_cancel), null)
                    .show();
        });

        // App Info
        findViewById(R.id.btnAbout).setOnClickListener(v -> showAboutDialog());
        findViewById(R.id.btnTerms).setOnClickListener(v -> showTermsDialog());
        findViewById(R.id.btnPrivacy).setOnClickListener(v -> showPrivacyDialog());
    }

    private void showChangeNameDialog() {
        EditText input = new EditText(this);
        input.setHint(getString(R.string.hint_enter_name));
        input.setText(prefs.getString("name", ""));
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_change_name_title))
                .setView(input)
                .setPositiveButton(getString(R.string.btn_save), (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        updateProfile(newName, null);
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private void showChangePhoneDialog() {
        EditText input = new EditText(this);
        input.setHint(getString(R.string.hint_enter_phone));
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setText(prefs.getString("phone", ""));
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_change_phone_title))
                .setView(input)
                .setPositiveButton(getString(R.string.btn_save), (d, w) -> {
                    String phone = input.getText().toString().trim();
                    if (!phone.isEmpty()) {
                        updateProfile(null, phone);
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private void updateProfile(String name, String phone) {
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject params = new JSONObject();
            params.put("user_id", userId);
            if (name != null)
                params.put("name", name);
            if (phone != null)
                params.put("phone", phone);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_UPDATE_PROFILE,
                    params,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                // Update local storage
                                if (name != null) {
                                    prefs.edit().putString("name", name).apply();
                                    Toast.makeText(this, "Name updated to: " + name, Toast.LENGTH_SHORT).show();
                                }
                                if (phone != null) {
                                    prefs.edit().putString("phone", phone).apply();
                                    Toast.makeText(this, "Phone updated", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String msg = response.optString("message", "Update failed");
                                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Parse error", e);
                        }
                    },
                    error -> {
                        Log.e(TAG, "Update error", error);
                        // Still save locally if network fails
                        if (name != null)
                            prefs.edit().putString("name", name).apply();
                        if (phone != null)
                            prefs.edit().putString("phone", phone).apply();
                        Toast.makeText(this, "Saved locally", Toast.LENGTH_SHORT).show();
                    });

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (Exception e) {
            Log.e(TAG, "JSON error", e);
        }
    }

    private void showChangePasswordDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);

        EditText oldPass = new EditText(this);
        oldPass.setHint("Current Password");
        oldPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(oldPass);

        EditText newPass = new EditText(this);
        newPass.setHint("New Password");
        newPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPass);

        EditText confirmPass = new EditText(this);
        confirmPass.setHint("Confirm Password");
        confirmPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmPass);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(layout)
                .setPositiveButton("Change", (d, w) -> {
                    String oldP = oldPass.getText().toString();
                    String newP = newPass.getText().toString();
                    String confirmP = confirmPass.getText().toString();

                    if (oldP.isEmpty()) {
                        Toast.makeText(this, "Enter current password", Toast.LENGTH_SHORT).show();
                    } else if (newP.length() < 6) {
                        Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    } else if (!newP.equals(confirmP)) {
                        Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                    } else {
                        changePassword(oldP, newP);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void changePassword(String oldPassword, String newPassword) {
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject params = new JSONObject();
            params.put("user_id", userId);
            params.put("old_password", oldPassword);
            params.put("new_password", newPassword);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_CHANGE_PASSWORD,
                    params,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                String msg = response.optString("message", "Failed to change password");
                                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Parse error", e);
                        }
                    },
                    error -> {
                        Log.e(TAG, "Password change error", error);
                        Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    });

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (Exception e) {
            Log.e(TAG, "JSON error", e);
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_about_title))
                .setMessage(getString(R.string.dialog_about_message))
                .setPositiveButton(getString(R.string.btn_ok), null)
                .show();
    }

    private void showTermsDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_terms_title))
                .setMessage(getString(R.string.dialog_terms_message))
                .setPositiveButton(getString(R.string.btn_ok), null)
                .show();
    }

    private void showPrivacyDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_privacy_title))
                .setMessage(getString(R.string.dialog_privacy_message))
                .setPositiveButton(getString(R.string.btn_ok), null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        settingsPrefs.edit()
                .putInt("preferred_category", spinnerCategory.getSelectedItemPosition())
                .putInt("logout_timer", spinnerLogoutTimer.getSelectedItemPosition())
                .apply();
    }
}
