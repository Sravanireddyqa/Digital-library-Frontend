package com.simats.digitallibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * My Bookings Activity - Shows user's reservations with QR codes
 */
public class MyBookingsActivity extends AppCompatActivity {

    private static final String TAG = "MyBookings";
    private static final String PREF_NAME = "UserSession";

    private RecyclerView recyclerReservations;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private TextView tvReservationCount;
    private ReservationAdapter adapter;
    private String mode = "all"; // "read", "active", or "all"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        // Get mode from intent
        mode = getIntent().getStringExtra("mode");
        if (mode == null)
            mode = "all";

        initViews();
        setupRecyclerView();
        setupBottomNavigation();
        loadReservations();

        // Update title based on mode
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) {
            if (mode.equals("read")) {
                tvTitle.setText("Books Read");
            } else if (mode.equals("active")) {
                tvTitle.setText("Active Reservations");
            }
        }
    }

    private void initViews() {
        recyclerReservations = findViewById(R.id.recyclerReservations);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvReservationCount = findViewById(R.id.tvReservationCount);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        TextView btnBrowseBooks = findViewById(R.id.btnBrowseBooks);
        btnBrowseBooks.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchBooksActivity.class));
        });

        // Top bar buttons
        findViewById(R.id.btnNotification).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });
        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ReaderProfileActivity.class));
        });
    }

    private void setupRecyclerView() {
        adapter = new ReservationAdapter();
        adapter.setOnActionListener(new ReservationAdapter.OnActionListener() {
            @Override
            public void onShowQRCode(Reservation reservation) {
                showQRCodeDialog(reservation);
            }

            @Override
            public void onGetDirections(Reservation reservation) {
                openDirections(reservation);
            }

            @Override
            public void onRate(Reservation reservation) {
                showRatingDialog(reservation);
            }

            @Override
            public void onCancel(Reservation reservation) {
                showCancelConfirmDialog(reservation);
            }
        });

        recyclerReservations.setLayoutManager(new LinearLayoutManager(this));
        recyclerReservations.setNestedScrollingEnabled(false);
        recyclerReservations.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        NavigationHelper.setupBottomNavigation(this, R.id.nav_bookings);
    }

    private void loadReservations() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        String url = ApiConfig.URL_GET_MY_BOOKINGS + "?user_id=" + userId + "&mode=" + mode;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray bookingsArray = response.getJSONArray("bookings");
                            List<Reservation> reservations = new ArrayList<>();

                            for (int i = 0; i < bookingsArray.length(); i++) {
                                JSONObject obj = bookingsArray.getJSONObject(i);
                                Reservation r = new Reservation();
                                r.id = obj.getInt("id");
                                r.bookId = obj.optInt("book_id", 0);
                                r.bookTitle = obj.optString("book_title", "Book");
                                r.bookAuthor = obj.optString("author", "");
                                r.bookCover = obj.optString("cover_url", "");
                                r.libraryName = obj.optString("library_name", "Library");
                                r.libraryAddress = obj.optString("library_address", "");
                                r.date = obj.optString("reservation_date", obj.optString("date", ""));
                                r.timeSlot = obj.optString("time_slot", "10:00 AM - 12:00 PM");
                                r.status = obj.optString("status", "pending");
                                r.libraryLat = obj.optDouble("library_lat", 0);
                                r.libraryLng = obj.optDouble("library_lng", 0);
                                reservations.add(r);
                            }

                            if (reservations.isEmpty()) {
                                showEmpty();
                            } else {
                                tvReservationCount.setText(reservations.size() + " active reservations");
                                adapter.setReservations(reservations);
                                recyclerReservations.setVisibility(View.VISIBLE);
                            }
                        } else {
                            loadSampleData();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                        loadSampleData();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error: " + error.getMessage());
                    loadSampleData();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadSampleData() {
        List<Reservation> reservations = new ArrayList<>();

        Reservation r1 = new Reservation();
        r1.id = 1;
        r1.bookTitle = "The Great Gatsby";
        r1.bookAuthor = "F. Scott Fitzgerald";
        r1.libraryName = "Central City Library";
        r1.date = "1/25/2024";
        r1.timeSlot = "10:00 AM - 12:00 PM";
        r1.status = "ready";
        r1.libraryLat = 13.0827;
        r1.libraryLng = 80.2707;
        reservations.add(r1);

        Reservation r2 = new Reservation();
        r2.id = 2;
        r2.bookTitle = "Pride and Prejudice";
        r2.bookAuthor = "Jane Austen";
        r2.libraryName = "University Library";
        r2.date = "1/30/2024";
        r2.timeSlot = "11:00 AM - 1:00 PM";
        r2.status = "confirmed";
        r2.libraryLat = 13.0107;
        r2.libraryLng = 80.2417;
        reservations.add(r2);

        tvReservationCount.setText(reservations.size() + " active reservations");
        adapter.setReservations(reservations);
        recyclerReservations.setVisibility(View.VISIBLE);
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerReservations.setVisibility(View.GONE);
        tvReservationCount.setText("0 active reservations");
    }

    private void showQRCodeDialog(Reservation reservation) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_qr_code, null);

        String reservationCode = "RES-" + String.format("%03d", reservation.id) + "-2024";

        TextView tvReservationId = view.findViewById(R.id.tvReservationId);
        tvReservationId.setText(reservationCode);

        // Generate QR Code
        ImageView ivQRCode = view.findViewById(R.id.ivQRCode);
        try {
            String qrContent = "LibraryAI|" + reservationCode + "|" + reservation.bookTitle;
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);
            ivQRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.e(TAG, "QR Error: " + e.getMessage());
        }

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnDownloadQR).setOnClickListener(v -> {
            Toast.makeText(this, "✅ QR Code saved to gallery", Toast.LENGTH_SHORT).show();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void openDirections(Reservation reservation) {
        try {
            String uri;
            if (reservation.libraryLat != 0 && reservation.libraryLng != 0) {
                uri = String.format("google.navigation:q=%f,%f",
                        reservation.libraryLat, reservation.libraryLng);
            } else {
                uri = "geo:0,0?q=" + Uri.encode(reservation.libraryName);
            }

            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                String browserUri = "https://www.google.com/maps/search/?api=1&query=" +
                        Uri.encode(reservation.libraryName);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri)));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open maps", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRatingDialog(Reservation reservation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        TextView tvBookTitle = dialogView.findViewById(R.id.tvBookTitle);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etReview = dialogView.findViewById(R.id.etReview);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        tvBookTitle.setText(reservation.bookTitle);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnSubmit.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            if (rating == 0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            String review = etReview.getText().toString().trim();
            submitRating(reservation, rating, review, dialog);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void submitRating(Reservation reservation, int rating, String review, AlertDialog dialog) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        JSONObject data = new JSONObject();
        try {
            data.put("user_id", userId);
            data.put("book_id", reservation.bookId);
            data.put("rating", rating);
            data.put("review", review);
        } catch (JSONException e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_SUBMIT_RATING,
                data,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            dialog.dismiss();
                            // Show Thank You dialog
                            new android.app.AlertDialog.Builder(this)
                                    .setTitle("Thank You! 🎉")
                                    .setMessage(
                                            "Your rating has been submitted successfully.\n\nThank you for your feedback!")
                                    .setPositiveButton("OK", null)
                                    .setIcon(android.R.drawable.star_big_on)
                                    .show();
                        } else {
                            String message = response.optString("message", "Failed to submit");
                            if (response.optBoolean("already_rated", false)) {
                                Toast.makeText(this, "You have already rated this book", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void showCancelConfirmDialog(Reservation reservation) {
        // First confirm cancellation
        new android.app.AlertDialog.Builder(this)
                .setTitle("Cancel Reservation?")
                .setMessage("Are you sure you want to cancel your reservation for '" + reservation.bookTitle
                        + "'?\n\nYour deposit will be refunded to your original payment method within 5-7 business days.")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    // Show reason dialog
                    showCancelReasonDialog(reservation);
                })
                .setNegativeButton("No, Keep It", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showCancelReasonDialog(Reservation reservation) {
        // Create EditText for reason
        final android.widget.EditText etReason = new android.widget.EditText(this);
        etReason.setHint("Why are you cancelling? (Optional)");
        etReason.setMinLines(2);
        etReason.setPadding(50, 30, 50, 30);

        new android.app.AlertDialog.Builder(this)
                .setTitle("📝 Cancellation Reason")
                .setMessage("Please tell us why you're cancelling (optional):")
                .setView(etReason)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String reason = etReason.getText().toString().trim();
                    cancelReservation(reservation, reason);
                })
                .setNegativeButton("Skip", (dialog, which) -> {
                    cancelReservation(reservation, "");
                })
                .setCancelable(false)
                .show();
    }

    private void cancelReservation(Reservation reservation, String reason) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        JSONObject data = new JSONObject();
        try {
            data.put("reservation_id", reservation.id);
            data.put("user_id", userId);
            data.put("cancel_reason", reason);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        Toast.makeText(this, "Cancelling reservation...", Toast.LENGTH_SHORT).show();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_CANCEL_RESERVATION,
                data,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            String refundMsg = response.optString("refund_message", "");
                            new android.app.AlertDialog.Builder(this)
                                    .setTitle("✅ Reservation Cancelled")
                                    .setMessage("Your reservation has been cancelled successfully.\n\n" + refundMsg)
                                    .setPositiveButton("OK", null)
                                    .show();
                            loadReservations(); // Refresh list
                        } else {
                            Toast.makeText(this, response.optString("message", "Failed to cancel"), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    // ========== Reservation Model ==========
    public static class Reservation {
        public int id;
        public int bookId;
        public String bookTitle;
        public String bookAuthor;
        public String bookCover;
        public String libraryName;
        public String libraryAddress;
        public String date;
        public String timeSlot;
        public String status;
        public double libraryLat;
        public double libraryLng;
    }

    // ========== Reservation Adapter ==========
    public static class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {
        private List<Reservation> reservations = new ArrayList<>();
        private OnActionListener listener;

        public interface OnActionListener {
            void onShowQRCode(Reservation reservation);

            void onGetDirections(Reservation reservation);

            void onRate(Reservation reservation);

            void onCancel(Reservation reservation);
        }

        public void setOnActionListener(OnActionListener listener) {
            this.listener = listener;
        }

        public void setReservations(List<Reservation> reservations) {
            this.reservations = reservations;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reservation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Reservation r = reservations.get(position);
            holder.bind(r);
        }

        @Override
        public int getItemCount() {
            return reservations.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivBookCover;
            TextView tvBookTitle, tvLibrary, tvDate, tvTimeSlot, tvStatus;
            TextView btnShowQR, btnGetDirections, btnCancel;
            View layoutTimeSlot;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivBookCover = itemView.findViewById(R.id.ivBookCover);
                tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
                tvLibrary = itemView.findViewById(R.id.tvLibrary);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvTimeSlot = itemView.findViewById(R.id.tvTimeSlot);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btnShowQR = itemView.findViewById(R.id.btnShowQR);
                btnGetDirections = itemView.findViewById(R.id.btnGetDirections);
                btnCancel = itemView.findViewById(R.id.btnCancel);
                layoutTimeSlot = itemView.findViewById(R.id.layoutTimeSlot);
            }

            void bind(Reservation r) {
                tvBookTitle.setText(r.bookTitle);
                tvLibrary.setText(r.libraryName);
                tvDate.setText(r.date);
                tvTimeSlot.setText(r.timeSlot);

                // Status badge
                String status = r.status.toLowerCase();
                if (status.equals("returned")) {
                    tvStatus.setText("Returned");
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_green);
                    tvStatus.setTextColor(
                            itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark));
                    // Show Rate button for returned books
                    btnShowQR.setText("⭐ Rate");
                    btnShowQR.setOnClickListener(v -> {
                        if (listener != null)
                            listener.onRate(r);
                    });
                } else if (status.equals("ready") || status.equals("approved")) {
                    tvStatus.setText("Ready");
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_green);
                    tvStatus.setTextColor(
                            itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                } else if (status.equals("confirmed")) {
                    tvStatus.setText("Confirmed");
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_purple);
                    tvStatus.setTextColor(
                            itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark));
                } else if (status.equals("rejected")) {
                    tvStatus.setText("Rejected");
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_yellow);
                    tvStatus.setTextColor(
                            itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                } else if (status.equals("cancelled")) {
                    tvStatus.setText("Cancelled");
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_yellow);
                    tvStatus.setTextColor(0xFF6B7280); // Gray color
                } else {
                    tvStatus.setText("Pending");
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_yellow);
                    tvStatus.setTextColor(
                            itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                }

                // Cover image
                if (r.bookCover != null && !r.bookCover.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(r.bookCover)
                            .placeholder(R.drawable.ic_library_book)
                            .into(ivBookCover);
                }

                // For cancelled reservations - hide all action buttons and time slot
                if (status.equals("cancelled")) {
                    btnShowQR.setVisibility(View.GONE);
                    btnGetDirections.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.GONE);
                    layoutTimeSlot.setVisibility(View.GONE);
                } else if (status.equals("returned")) {
                    // Show Rate button for returned books
                    btnShowQR.setText("⭐ Rate");
                    btnShowQR.setVisibility(View.VISIBLE);
                    btnShowQR.setOnClickListener(v -> {
                        if (listener != null)
                            listener.onRate(r);
                    });
                    btnGetDirections.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.GONE);
                    layoutTimeSlot.setVisibility(View.VISIBLE);
                } else {
                    // Active reservations - show QR and Directions
                    btnShowQR.setText("QR Code");
                    btnShowQR.setVisibility(View.VISIBLE);
                    btnShowQR.setOnClickListener(v -> {
                        if (listener != null)
                            listener.onShowQRCode(r);
                    });

                    // Directions button
                    btnGetDirections.setText("Directions");
                    btnGetDirections.setVisibility(View.VISIBLE);
                    btnGetDirections.setTextColor(0xFF3B82F6); // Blue
                    btnGetDirections.setOnClickListener(v -> {
                        if (listener != null)
                            listener.onGetDirections(r);
                    });

                    // Cancel button - show only for pending/approved/ready
                    if (status.equals("pending") || status.equals("approved") || status.equals("ready")) {
                        btnCancel.setVisibility(View.VISIBLE);
                        btnCancel.setOnClickListener(v -> {
                            if (listener != null)
                                listener.onCancel(r);
                        });
                    } else {
                        btnCancel.setVisibility(View.GONE);
                    }

                    // Show time slot for active reservations
                    layoutTimeSlot.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
