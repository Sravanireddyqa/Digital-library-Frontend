package com.simats.digitallibrary;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * In-App Credential Manager
 * Saves and manages user login credentials locally
 * Separates admin and user credentials
 */
public class CredentialManager {

    private static final String PREF_NAME = "SavedCredentials";
    private static final String KEY_USER_ACCOUNTS = "saved_user_accounts";
    private static final String KEY_ADMIN_ACCOUNTS = "saved_admin_accounts";
    private static final String KEY_OLD_ACCOUNTS = "saved_accounts"; // Old key for backward compatibility

    private final SharedPreferences prefs;

    public CredentialManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        migrateOldAccounts(); // Migrate old accounts to new format
    }

    /**
     * Migrate old saved accounts to user accounts (one-time migration)
     */
    private void migrateOldAccounts() {
        String oldJson = prefs.getString(KEY_OLD_ACCOUNTS, null);
        if (oldJson != null && !oldJson.equals("[]")) {
            // Move old accounts to user accounts
            String currentUserAccounts = prefs.getString(KEY_USER_ACCOUNTS, "[]");
            if (currentUserAccounts.equals("[]")) {
                // Only migrate if user accounts are empty
                prefs.edit()
                        .putString(KEY_USER_ACCOUNTS, oldJson)
                        .remove(KEY_OLD_ACCOUNTS)
                        .apply();
            } else {
                // Just remove old key
                prefs.edit().remove(KEY_OLD_ACCOUNTS).apply();
            }
        }
    }

    /**
     * Save a credential (email + password) for specific role
     */
    public void saveCredential(String email, String password, String role) {
        String key = "admin".equalsIgnoreCase(role) ? KEY_ADMIN_ACCOUNTS : KEY_USER_ACCOUNTS;
        List<SavedAccount> accounts = getAccountsByKey(key);

        // Remove existing entry for this email (if any)
        accounts.removeIf(a -> a.email.equalsIgnoreCase(email));

        // Add new entry at the top
        accounts.add(0, new SavedAccount(email, password));

        // Keep only last 5 accounts
        if (accounts.size() > 5) {
            accounts = accounts.subList(0, 5);
        }

        // Save to preferences
        saveAccountsList(accounts, key);
    }

    /**
     * Save credential (backward compatibility - defaults to user)
     */
    public void saveCredential(String email, String password) {
        saveCredential(email, password, "user");
    }

    /**
     * Get saved accounts for specific role
     */
    public List<SavedAccount> getSavedAccounts(String role) {
        String key = "admin".equalsIgnoreCase(role) ? KEY_ADMIN_ACCOUNTS : KEY_USER_ACCOUNTS;
        return getAccountsByKey(key);
    }

    /**
     * Get all saved accounts (for backward compatibility - returns user accounts)
     */
    public List<SavedAccount> getSavedAccounts() {
        return getSavedAccounts("user");
    }

    private List<SavedAccount> getAccountsByKey(String key) {
        List<SavedAccount> accounts = new ArrayList<>();
        String json = prefs.getString(key, "[]");

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                accounts.add(new SavedAccount(
                        obj.getString("email"),
                        obj.getString("password")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return accounts;
    }

    /**
     * Check if there are any saved accounts for specific role
     */
    public boolean hasSavedAccounts(String role) {
        return !getSavedAccounts(role).isEmpty();
    }

    /**
     * Check if there are any saved accounts (backward compatibility - user)
     */
    public boolean hasSavedAccounts() {
        return hasSavedAccounts("user");
    }

    /**
     * Delete a saved account
     */
    public void deleteAccount(String email, String role) {
        String key = "admin".equalsIgnoreCase(role) ? KEY_ADMIN_ACCOUNTS : KEY_USER_ACCOUNTS;
        List<SavedAccount> accounts = getAccountsByKey(key);
        accounts.removeIf(a -> a.email.equalsIgnoreCase(email));
        saveAccountsList(accounts, key);
    }

    /**
     * Clear all saved accounts
     */
    public void clearAll() {
        prefs.edit().clear().apply();
    }

    private void saveAccountsList(List<SavedAccount> accounts, String key) {
        JSONArray array = new JSONArray();
        for (SavedAccount account : accounts) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("email", account.email);
                obj.put("password", account.password);
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(key, array.toString()).apply();
    }

    /**
     * Saved account data class
     */
    public static class SavedAccount {
        public final String email;
        public final String password;

        public SavedAccount(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getMaskedPassword() {
            return "••••••••";
        }
    }
}
