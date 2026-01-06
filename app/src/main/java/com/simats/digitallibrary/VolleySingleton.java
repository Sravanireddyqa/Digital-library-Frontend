package com.simats.digitallibrary;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton class for Volley RequestQueue
 * ========================================
 * Use this for all network requests in the app
 * 
 * Usage:
 * VolleySingleton.getInstance(context).addToRequestQueue(request);
 */
public class VolleySingleton {

    private static VolleySingleton instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private VolleySingleton(Context context) {
        ctx = context.getApplicationContext();
        requestQueue = getRequestQueue();
    }

    /**
     * Get singleton instance
     */
    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    /**
     * Get the RequestQueue
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx);
        }
        return requestQueue;
    }

    /**
     * Add request to queue with retry policy
     */
    public <T> void addToRequestQueue(Request<T> request) {
        request.setRetryPolicy(new DefaultRetryPolicy(
                ApiConfig.TIMEOUT_MS,
                ApiConfig.MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(request);
    }

    /**
     * Cancel all requests with tag
     */
    public void cancelRequests(String tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }
}
