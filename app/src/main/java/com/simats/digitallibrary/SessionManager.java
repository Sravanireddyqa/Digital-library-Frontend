package com.simats.digitallibrary;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager
 * Manages user login session and preferences
 */
public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_ROLE = "userRole";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(int userId, String email, String name, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_ROLE, role);
        editor.commit();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get user ID
     */
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    /**
     * Get user email
     */
    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Get user name
     */
    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    /**
     * Get user role
     */
    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, "");
    }

    /**
     * Clear session (logout)
     */
    public void logoutUser() {
        editor.clear();
        editor.commit();
    }
}
