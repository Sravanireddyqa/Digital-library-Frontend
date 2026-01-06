package com.simats.digitallibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONException;
import org.json.JSONObject;

public class FirebaseTokenManager {
    private static final String TAG = "FirebaseTokenManager";
    private static final String PREFS_NAME = "fcm_prefs";
    private static final String KEY_FCM_TOKEN = "fcm_token";
    private static final String KEY_TOKEN_SENT = "token_sent_to_server";

    private final Context context;
    private final SharedPreferences prefs;

    public FirebaseTokenManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get FCM token and send to server
     */
    public void registerToken(int userId) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);

                    // Save token locally
                    saveToken(token);

                    // Send token to server
                    sendTokenToServer(userId, token);
                });
    }

    /**
     * Save token locally
     */
    private void saveToken(String token) {
        prefs.edit()
                .putString(KEY_FCM_TOKEN, token)
                .putBoolean(KEY_TOKEN_SENT, false)
                .apply();
    }

    /**
     * Get saved token
     */
    public String getSavedToken() {
        return prefs.getString(KEY_FCM_TOKEN, null);
    }

    /**
     * Check if token was sent to server
     */
    public boolean isTokenSentToServer() {
        return prefs.getBoolean(KEY_TOKEN_SENT, false);
    }

    /**
     * Send token to backend server
     */
    private void sendTokenToServer(int userId, String token) {
        if (userId <= 0) {
            Log.w(TAG, "Invalid user ID, cannot register token");
            return;
        }

        try {
            JSONObject params = new JSONObject();
            params.put("user_id", userId);
            params.put("fcm_token", token);
            params.put("device_info", android.os.Build.MODEL + " " + android.os.Build.VERSION.RELEASE);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.URL_REGISTER_FCM_TOKEN,
                    params,
                    response -> {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                Log.d(TAG, "Token successfully registered on server");
                                prefs.edit().putBoolean(KEY_TOKEN_SENT, true).apply();
                            } else {
                                Log.e(TAG, "Failed to register token: " + response.getString("message"));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error", e);
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error sending token to server: " + error.getMessage());
                        // Store for retry later
                        prefs.edit().putBoolean(KEY_TOKEN_SENT, false).apply();
                    });

            VolleySingleton.getInstance(context).addToRequestQueue(request);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating request", e);
        }
    }

    /**
     * Clear token (on logout)
     */
    public void clearToken() {
        prefs.edit()
                .remove(KEY_FCM_TOKEN)
                .remove(KEY_TOKEN_SENT)
                .apply();
    }
}
