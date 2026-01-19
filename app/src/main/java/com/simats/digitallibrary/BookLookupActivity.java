package com.simats.digitallibrary;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BookLookupActivity extends AppCompatActivity {

    private static final String TAG = "BookLookupActivity";

    // UI Components
    private EditText etBarcode;
    private AppCompatButton btnLookUp, btnScanAnother, btnUpdateBook;
    private CardView cardResult;
    private LinearLayout cardManualEntry, cardCameraScan;

    // Result Views
    private TextView tvBookTitle, tvBookAuthor, tvBookIsbn, tvBookCategory, tvBookCopies, tvBookLocation;

    // For reservation mode
    private boolean isReservationMode = false;
    private int currentReservationId = 0;
    private String currentReservationStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_lookup);

        initViews();
        setupListeners();
    }

    private void initViews() {
        // Input Section
        etBarcode = findViewById(R.id.etBarcode);
        btnLookUp = findViewById(R.id.btnLookUp);
        cardManualEntry = findViewById(R.id.cardManualEntry);
        cardCameraScan = findViewById(R.id.cardCameraScan);

        // Result Section
        cardResult = findViewById(R.id.cardResult);
        tvBookTitle = findViewById(R.id.tvBookTitle);
        tvBookAuthor = findViewById(R.id.tvBookAuthor);
        tvBookIsbn = findViewById(R.id.tvBookIsbn);
        tvBookCategory = findViewById(R.id.tvBookCategory);
        tvBookCopies = findViewById(R.id.tvBookCopies);
        tvBookLocation = findViewById(R.id.tvBookLocation);

        btnScanAnother = findViewById(R.id.btnScanAnother);
        btnUpdateBook = findViewById(R.id.btnUpdateBook);
    }

    private void setupListeners() {
        // Mode Selection (Visual Toggle)
        cardManualEntry.setOnClickListener(v -> setMode(true));
        cardCameraScan.setOnClickListener(v -> setMode(false));

        // Look Up Button
        btnLookUp.setOnClickListener(v -> lookupCode());

        // Scan Another Button
        btnScanAnother.setOnClickListener(v -> {
            cardResult.setVisibility(View.GONE);
            etBarcode.setText("");
            etBarcode.requestFocus();
            isReservationMode = false;
            resetButtonsToDefault();
        });

        // Update/Action Button
        btnUpdateBook.setOnClickListener(v -> {
            if (isReservationMode) {
                handleReservationAction();
            } else {
                Toast.makeText(this, "Update feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetButtonsToDefault() {
        btnUpdateBook.setText("Update Book\nDetails");
        btnUpdateBook.setBackgroundResource(R.drawable.bg_button_primary);
    }

    private void setMode(boolean isManual) {
        if (isManual) {
            cardManualEntry.setBackgroundResource(R.drawable.bg_manual_entry_selected);
            cardCameraScan.setBackgroundResource(R.drawable.bg_camera_scan_normal);
            Toast.makeText(this, "Manual Entry Mode", Toast.LENGTH_SHORT).show();
        } else {
            cardCameraScan.setBackgroundResource(R.drawable.bg_manual_entry_selected);
            cardManualEntry.setBackgroundResource(R.drawable.bg_camera_scan_normal);
            Toast.makeText(this, "Camera Scan feature starting...", Toast.LENGTH_SHORT).show();
        }
    }

    private void lookupCode() {
        String barcode = etBarcode.getText().toString().trim();

        if (barcode.isEmpty()) {
            etBarcode.setError("Please enter a barcode or QR code");
            return;
        }

        // Check if it's a reservation QR code (contains "RES-")
        if (barcode.contains("RES-") || barcode.contains("LibraryAI|RES")) {
            lookupReservation(barcode);
        } else {
            lookupBook(barcode);
        }
    }

    private void lookupBook(String barcode) {
        isReservationMode = false;
        btnLookUp.setEnabled(false);
        btnLookUp.setText("Searching...");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("barcode", barcode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = ApiConfig.BASE_URL + "book_lookup.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    btnLookUp.setEnabled(true);
                    btnLookUp.setText("Look Up Book");
                    handleBookResponse(response);
                },
                error -> {
                    btnLookUp.setEnabled(true);
                    btnLookUp.setText("Look Up Book");

                    String errorMsg = "Connection failed";
                    if (error.networkResponse != null) {
                        errorMsg = "Error: " + error.networkResponse.statusCode;
                        if (error.networkResponse.statusCode == 404) {
                            errorMsg = "Book not found";
                        }
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Volley Error: " + error.toString());
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void lookupReservation(String qrCode) {
        isReservationMode = true;
        btnLookUp.setEnabled(false);
        btnLookUp.setText("Looking up reservation...");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("qr_code", qrCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = ApiConfig.BASE_URL + "lookup_reservation.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    btnLookUp.setEnabled(true);
                    btnLookUp.setText("Look Up Book");
                    handleReservationResponse(response);
                },
                error -> {
                    btnLookUp.setEnabled(true);
                    btnLookUp.setText("Look Up Book");

                    String errorMsg = "Connection failed";
                    if (error.networkResponse != null) {
                        errorMsg = "Error: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Reservation Lookup Error: " + error.toString());
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void handleBookResponse(JSONObject response) {
        try {
            boolean success = response.getBoolean("success");
            if (success) {
                JSONObject book = response.getJSONObject("book");

                // Populate fields
                tvBookTitle.setText(book.optString("title", "N/A"));
                tvBookAuthor.setText(book.optString("author", "N/A"));
                tvBookIsbn.setText(book.optString("isbn", "N/A"));
                tvBookCategory.setText(book.optString("category", "N/A"));
                tvBookCopies.setText(String.valueOf(book.optInt("copies", 0)));
                tvBookLocation.setText(book.optString("shelf_location", "Main Stack"));

                // Show book mode buttons
                btnUpdateBook.setText("Update Book\nDetails");
                btnUpdateBook.setBackgroundResource(R.drawable.bg_button_primary);

                cardResult.setVisibility(View.VISIBLE);

            } else {
                String message = response.getString("message");
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON Parse Error", e);
            Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleReservationResponse(JSONObject response) {
        try {
            boolean success = response.getBoolean("success");
            if (success) {
                JSONObject reservation = response.getJSONObject("reservation");
                JSONArray actions = response.optJSONArray("available_actions");

                currentReservationId = reservation.getInt("id");
                currentReservationStatus = reservation.optString("status", "unknown");

                // Populate fields with reservation data
                tvBookTitle.setText(reservation.optString("book_title", "N/A"));
                tvBookAuthor.setText(reservation.optString("book_author", "N/A"));

                // Show user info instead of ISBN
                String userName = reservation.optString("user_name", "Unknown");
                tvBookIsbn.setText("User: " + userName);

                // Show status instead of category
                String status = reservation.optString("status", "N/A").toUpperCase();
                tvBookCategory.setText("Status: " + status);

                // Show pickup date
                String pickupDate = reservation.optString("pickup_date", "N/A");
                tvBookCopies.setText(pickupDate);

                // Show time slot
                String timeSlot = reservation.optString("time_slot", "N/A");
                tvBookLocation.setText(timeSlot);

                // Update button based on available actions
                if (actions != null && actions.length() > 0) {
                    String primaryAction = actions.getString(0);
                    updateActionButton(primaryAction);
                } else {
                    // No actions available (completed or cancelled)
                    btnUpdateBook.setText("Completed ✓");
                    btnUpdateBook.setEnabled(false);
                    btnUpdateBook.setBackgroundResource(R.drawable.bg_button_secondary);
                }

                cardResult.setVisibility(View.VISIBLE);

            } else {
                String message = response.getString("message");
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                // Check if it's actually a book QR, not reservation
                if (response.optBoolean("is_book_qr", false)) {
                    String barcode = etBarcode.getText().toString().trim();
                    lookupBook(barcode);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON Parse Error", e);
            Toast.makeText(this, "Error parsing reservation data", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateActionButton(String action) {
        btnUpdateBook.setEnabled(true);

        switch (action.toLowerCase()) {
            case "pickup":
                btnUpdateBook.setText("📦 Mark as\nPicked Up");
                btnUpdateBook.setBackgroundResource(R.drawable.bg_button_success);
                break;
            case "return":
                btnUpdateBook.setText("🔄 Mark as\nReturned");
                btnUpdateBook.setBackgroundResource(R.drawable.bg_button_warning);
                break;
            case "approve":
                btnUpdateBook.setText("✓ Approve");
                btnUpdateBook.setBackgroundResource(R.drawable.bg_button_success);
                break;
            case "reject":
                btnUpdateBook.setText("✗ Reject");
                btnUpdateBook.setBackgroundResource(R.drawable.bg_button_danger);
                break;
            default:
                btnUpdateBook.setText("Process");
                btnUpdateBook.setBackgroundResource(R.drawable.bg_button_primary);
        }
    }

    private void handleReservationAction() {
        if (currentReservationId == 0) {
            Toast.makeText(this, "No reservation selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String newStatus = "";
        String actionText = btnUpdateBook.getText().toString().toLowerCase();

        if (actionText.contains("picked up")) {
            newStatus = "picked_up";
        } else if (actionText.contains("returned")) {
            newStatus = "returned";
        } else if (actionText.contains("approve")) {
            newStatus = "approved";
        } else if (actionText.contains("reject")) {
            newStatus = "rejected";
        }

        if (newStatus.isEmpty()) {
            Toast.makeText(this, "Unknown action", Toast.LENGTH_SHORT).show();
            return;
        }

        updateReservationStatus(currentReservationId, newStatus);
    }

    private void updateReservationStatus(int reservationId, String newStatus) {
        btnUpdateBook.setEnabled(false);
        btnUpdateBook.setText("Processing...");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("reservation_id", reservationId);
            jsonBody.put("status", newStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = ApiConfig.BASE_URL + "update_reservation.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            String successMsg = "";
                            switch (newStatus) {
                                case "picked_up":
                                    successMsg = "✅ Book marked as Picked Up!";
                                    break;
                                case "returned":
                                    successMsg = "✅ Book marked as Returned!";
                                    break;
                                case "approved":
                                    successMsg = "✅ Reservation Approved!";
                                    break;
                                case "rejected":
                                    successMsg = "❌ Reservation Rejected";
                                    break;
                                default:
                                    successMsg = "✅ Status updated!";
                            }
                            Toast.makeText(this, successMsg, Toast.LENGTH_LONG).show();

                            // Update UI
                            btnUpdateBook.setText("Completed ✓");
                            btnUpdateBook.setEnabled(false);
                            tvBookCategory.setText("Status: " + newStatus.toUpperCase().replace("_", " "));

                        } else {
                            String message = response.optString("message", "Update failed");
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            btnUpdateBook.setEnabled(true);
                            lookupReservation(etBarcode.getText().toString().trim());
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Response parse error", e);
                        btnUpdateBook.setEnabled(true);
                    }
                },
                error -> {
                    btnUpdateBook.setEnabled(true);
                    lookupReservation(etBarcode.getText().toString().trim());
                    Toast.makeText(this, "Network error, status may not have updated", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Update error: " + error.toString());
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
