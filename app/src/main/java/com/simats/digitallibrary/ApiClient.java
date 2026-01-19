package com.simats.digitallibrary;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton class for Volley RequestQueue
 * Provides centralized network request management
 */
public class ApiClient {

    private static ApiClient instance;
    private RequestQueue requestQueue;
    private static Context context;

    private ApiClient(Context ctx) {
        context = ctx.getApplicationContext();
        requestQueue = getRequestQueue();
    }

    /**
     * Get singleton instance of ApiClient
     */
    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    /**
     * Get the RequestQueue
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        return requestQueue;
    }

    /**
     * Add request to queue with default retry policy
     */
    public <T> void addToRequestQueue(Request<T> request) {
        // Set retry policy with timeout from ApiConfig
        request.setRetryPolicy(new DefaultRetryPolicy(
                ApiConfig.TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(request);
    }

    /**
     * Add request with custom tag for cancellation
     */
    public <T> void addToRequestQueue(Request<T> request, String tag) {
        request.setTag(tag);
        request.setRetryPolicy(new DefaultRetryPolicy(
                ApiConfig.TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(request);
    }

    /**
     * Cancel all requests with the given tag
     */
    public void cancelRequests(String tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }

    /**
     * Cancel all pending requests
     */
    public void cancelAllRequests() {
        if (requestQueue != null) {
            requestQueue.cancelAll(request -> true);
        }
    }
}
