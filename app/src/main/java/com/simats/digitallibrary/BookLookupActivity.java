package com.simats.digitallibrary;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

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
        btnLookUp.setOnClickListener(v -> lookupBook());

        // Scan Another Button
        btnScanAnother.setOnClickListener(v -> {
            cardResult.setVisibility(View.GONE);
            etBarcode.setText("");
            etBarcode.requestFocus();
        });

        // Update Button (Placeholder)
        btnUpdateBook.setOnClickListener(v -> {
            Toast.makeText(this, "Update feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void setMode(boolean isManual) {
        if (isManual) {
            // Highlight Manual
            cardManualEntry.setBackgroundResource(R.drawable.bg_manual_entry_selected);
            cardCameraScan.setBackgroundResource(R.drawable.bg_camera_scan_normal);
            // Hide camera, show input (already designed this way for this demo)
            Toast.makeText(this, "Manual Entry Mode", Toast.LENGTH_SHORT).show();
        } else {
            // Highlight Camera
            cardCameraScan.setBackgroundResource(R.drawable.bg_manual_entry_selected); // Use selected bg for visual
                                                                                       // feedback
            cardManualEntry.setBackgroundResource(R.drawable.bg_camera_scan_normal);
            Toast.makeText(this, "Camera Scan feature starting...", Toast.LENGTH_SHORT).show();
            // TODO: Integrate ML Kit or ZXing for actual camera scanning
        }
    }

    private void lookupBook() {
        String barcode = etBarcode.getText().toString().trim();

        if (barcode.isEmpty()) {
            etBarcode.setError("Please enter a barcode");
            return;
        }

        btnLookUp.setEnabled(false);
        btnLookUp.setText("Searching...");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("barcode", barcode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Use the new endpoint
        String url = ApiConfig.BASE_URL + "book_lookup.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    btnLookUp.setEnabled(true);
                    btnLookUp.setText("Look Up Book");
                    handleLookUpResponse(response);
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

    private void handleLookUpResponse(JSONObject response) {
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

                // Optional: Location (if your API returns it)
                String location = book.optString("shelf_location", "Main Stack");
                tvBookLocation.setText(location);

                // Show Result Card
                cardResult.setVisibility(View.VISIBLE);

                // Scroll to bottom to see result
                // (NestedScrollView handles this automatically usually, but can be forced if
                // needed)

            } else {
                String message = response.getString("message");
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON Parse Error", e);
            Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show();
        }
    }
}
