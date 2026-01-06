package com.simats.digitallibrary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * QR Code Book Scanner Activity
 * Scan QR codes using camera or enter manually to look up books
 */
public class QRScannerActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private CardView cardCameraScan, cardManualInput, cardBookResult;
    private Button btnStartScan, btnLookUp, btnSaveBook, btnEditBook, btnDeleteBook;
    private EditText etQRCode;
    private ProgressBar progressBar;

    // Book result views
    private ImageView ivBookCover;
    private TextView tvBookTitle, tvBookAuthor, tvBookCategory, tvAvailability;
    private View viewAvailabilityDot;

    private Book currentBook;

    // QR Scanner launcher
    private final ActivityResultLauncher<ScanOptions> qrScannerLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String scannedCode = result.getContents();
                    etQRCode.setText(scannedCode);
                    lookUpBook(scannedCode);
                } else {
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        cardCameraScan = findViewById(R.id.cardCameraScan);
        cardManualInput = findViewById(R.id.cardManualInput);
        cardBookResult = findViewById(R.id.cardBookResult);
        btnStartScan = findViewById(R.id.btnStartScan);
        etQRCode = findViewById(R.id.etQRCode);
        btnLookUp = findViewById(R.id.btnLookUp);
        progressBar = findViewById(R.id.progressBar);

        // Book result views
        ivBookCover = findViewById(R.id.ivBookCover);
        tvBookTitle = findViewById(R.id.tvBookTitle);
        tvBookAuthor = findViewById(R.id.tvBookAuthor);
        tvBookCategory = findViewById(R.id.tvBookCategory);
        tvAvailability = findViewById(R.id.tvAvailability);
        viewAvailabilityDot = findViewById(R.id.viewAvailabilityDot);
        btnSaveBook = findViewById(R.id.btnSaveBook);
        btnEditBook = findViewById(R.id.btnEditBook);
        btnDeleteBook = findViewById(R.id.btnDeleteBook);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Start Camera Scan
        btnStartScan.setOnClickListener(v -> startQRScanner());

        // Tap on camera card also triggers scan
        cardCameraScan.setOnClickListener(v -> startQRScanner());

        // Look Up Button - Manual entry
        btnLookUp.setOnClickListener(v -> {
            String qrCode = etQRCode.getText().toString().trim();
            if (qrCode.isEmpty()) {
                etQRCode.setError("Please enter QR code");
                return;
            }
            lookUpBook(qrCode);
        });

        // Click on book result card to open details
        cardBookResult.setOnClickListener(v -> {
            if (currentBook != null && currentBook.getId() > 0) {
                Intent intent = new Intent(this, BookDetailsActivity.class);
                intent.putExtra("book_id", currentBook.getId());
                startActivity(intent);
            }
        });

        // Save Book
        btnSaveBook.setOnClickListener(v -> {
            if (currentBook != null) {
                saveBook(currentBook);
            }
        });

        // Edit Book
        btnEditBook.setOnClickListener(v -> {
            if (currentBook != null) {
                Toast.makeText(this, "Edit: " + currentBook.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        // Delete Book
        btnDeleteBook.setOnClickListener(v -> {
            if (currentBook != null) {
                Toast.makeText(this, "Delete: " + currentBook.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startQRScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Point camera at QR code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setCaptureActivity(PortraitCaptureActivity.class);
        qrScannerLauncher.launch(options);
    }

    private void lookUpBook(String qrCode) {
        progressBar.setVisibility(View.VISIBLE);
        cardBookResult.setVisibility(View.GONE);

        String url = ApiConfig.URL_BOOK_LOOKUP + "?qr_code=" + qrCode;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            String type = response.optString("type", "book");

                            if ("reservation".equals(type)) {
                                // Reservation QR code - show reservation details
                                JSONObject reservationData = response.getJSONObject("reservation");
                                JSONObject bookData = response.getJSONObject("book");
                                displayReservationResult(reservationData, bookData);
                            } else {
                                // Regular book lookup
                                JSONObject bookData = response.getJSONObject("book");
                                displayBookResult(bookData);
                            }
                        } else {
                            Toast.makeText(this,
                                    response.optString("message", "Not found"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    String errorMsg = "Network error. Please try again.";
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        errorMsg = "QR code not found";
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void displayReservationResult(JSONObject reservationData, JSONObject bookData) throws JSONException {
        currentBook = new Book();
        currentBook.setId(bookData.optInt("id", 0));
        currentBook.setTitle(bookData.optString("title", "Unknown Title"));
        currentBook.setAuthor(bookData.optString("author", "Unknown Author"));
        currentBook.setCategory(bookData.optString("category", "General"));
        currentBook.setIsbn(bookData.optString("isbn", ""));
        currentBook.setStock(bookData.optInt("stock", 0));
        currentBook.setCoverUrl(bookData.optString("cover_url", ""));

        // Update UI
        tvBookTitle.setText(currentBook.getTitle());
        tvBookAuthor.setText("by " + currentBook.getAuthor());
        tvBookCategory.setText(currentBook.getCategory());

        // Show reservation status instead of availability
        String status = reservationData.optString("status", "pending");
        String userName = reservationData.optString("user_name", "");
        String reservationDate = reservationData.optString("reservation_date", "");

        String statusText = "Reserved by: " + userName;
        if (!reservationDate.isEmpty()) {
            statusText += " on " + reservationDate;
        }
        statusText += " (" + status.toUpperCase() + ")";

        tvAvailability.setText(statusText);

        if ("approved".equals(status)) {
            tvAvailability.setTextColor(getColor(android.R.color.holo_green_dark));
            viewAvailabilityDot.setBackgroundResource(R.drawable.bg_dot_green);
        } else if ("rejected".equals(status)) {
            tvAvailability.setTextColor(getColor(android.R.color.holo_red_dark));
            viewAvailabilityDot.setBackgroundResource(R.drawable.bg_dot_red);
        } else {
            tvAvailability.setTextColor(getColor(android.R.color.holo_orange_dark));
            viewAvailabilityDot.setBackgroundResource(R.drawable.bg_dot_yellow);
        }

        // Load cover image
        String coverUrl = currentBook.getCoverUrl();
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this)
                    .load(coverUrl)
                    .centerCrop()
                    .into(ivBookCover);
        }

        cardBookResult.setVisibility(View.VISIBLE);
    }

    private void displayBookResult(JSONObject bookData) throws JSONException {
        currentBook = new Book();
        currentBook.setId(bookData.optInt("id", 0));
        currentBook.setTitle(bookData.optString("title", "Unknown Title"));
        currentBook.setAuthor(bookData.optString("author", "Unknown Author"));
        currentBook.setCategory(bookData.optString("category", "General"));
        currentBook.setIsbn(bookData.optString("isbn", ""));
        currentBook.setStock(bookData.optInt("stock", 0));
        currentBook.setCoverUrl(bookData.optString("cover_url", ""));

        // Update UI
        tvBookTitle.setText(currentBook.getTitle());
        tvBookAuthor.setText("by " + currentBook.getAuthor());
        tvBookCategory.setText(currentBook.getCategory());

        // Availability
        boolean isAvailable = currentBook.getStock() > 0;
        if (isAvailable) {
            tvAvailability.setText("Available (" + currentBook.getStock() + " copies)");
            tvAvailability.setTextColor(getColor(android.R.color.holo_green_dark));
            viewAvailabilityDot.setBackgroundResource(R.drawable.bg_dot_green);
        } else {
            tvAvailability.setText("Not Available");
            tvAvailability.setTextColor(getColor(android.R.color.holo_red_dark));
            viewAvailabilityDot.setBackgroundResource(R.drawable.bg_dot_red);
        }

        // Load cover image
        String coverUrl = currentBook.getCoverUrl();
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this)
                    .load(coverUrl)
                    .centerCrop()
                    .into(ivBookCover);
        }

        cardBookResult.setVisibility(View.VISIBLE);
    }

    private void saveBook(Book book) {
        Toast.makeText(this, "Book saved: " + book.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
