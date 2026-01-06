package com.simats.digitallibrary;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Reservation Flow Activity with Razorpay Payment
 * 3-step process: Select Library -> Select Date/Time -> Pay & Confirm
 */
public class ReservationFlowActivity extends AppCompatActivity implements PaymentResultListener {

    private static final String TAG = "ReservationFlow";
    private static final String PREF_NAME = "UserSession";

    // Current step (1, 2, or 3)
    private int currentStep = 1;

    // Views
    private View layoutStep1, layoutStep2, layoutStep3;
    private TextView tvStep1, tvStep2, tvStep3;
    private TextView btnPrevious, btnContinue;
    private TextView tvBookTitle, tvAuthor;
    private ImageView ivBookCover;
    private TextView tvSelectedDate, tvReturnDate, tvConfirmLibrary, tvConfirmDate, tvConfirmTimeSlot,
            tvConfirmReturnDate;
    private TextView tvRentalFee, tvTotal, tvDeposit;
    private RecyclerView recyclerLibraries, recyclerTimeSlots;

    // Data
    private int bookId;
    private String bookTitle, bookAuthor, bookCoverUrl;
    private double bookPrice = 0;
    private int selectedLibraryId = -1;
    private String selectedLibraryName = "";
    private String selectedDate = "";
    private String selectedReturnDate = "";
    private String selectedTimeSlot = "";
    private int selectedTimeSlotIndex = -1;

    // Adapters
    private LibrarySelectAdapter libraryAdapter;
    private TimeSlotAdapter timeSlotAdapter;

    // Sample time slots
    private final String[] TIME_SLOTS = {
            "9:00 AM - 11:00 AM",
            "11:00 AM - 1:00 PM",
            "1:00 PM - 3:00 PM",
            "3:00 PM - 5:00 PM",
            "5:00 PM - 7:00 PM"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_flow);

        // Get book info from intent
        bookId = getIntent().getIntExtra("book_id", 0);
        bookTitle = getIntent().getStringExtra("book_title");
        bookAuthor = getIntent().getStringExtra("book_author");
        bookCoverUrl = getIntent().getStringExtra("book_cover");
        bookPrice = getIntent().getDoubleExtra("book_price", 0);

        if (bookId == 0) {
            Toast.makeText(this, "Book not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerViews();
        setupClickListeners();
        updateStep(1);
        loadBookInfo();
        loadLibraries();
    }

    private void initViews() {
        // Step layouts
        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        layoutStep3 = findViewById(R.id.layoutStep3);

        // Step indicators
        tvStep1 = findViewById(R.id.tvStep1);
        tvStep2 = findViewById(R.id.tvStep2);
        tvStep3 = findViewById(R.id.tvStep3);

        // Buttons
        btnPrevious = findViewById(R.id.btnPrevious);
        btnContinue = findViewById(R.id.btnContinue);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Top bar buttons - Notification and Profile
        findViewById(R.id.btnNotification).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });
        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ReaderProfileActivity.class));
        });

        // Book info
        ivBookCover = findViewById(R.id.ivBookCover);
        tvBookTitle = findViewById(R.id.tvBookTitle);
        tvAuthor = findViewById(R.id.tvAuthor);

        // Step 2
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvReturnDate = findViewById(R.id.tvReturnDate);
        findViewById(R.id.layoutDatePicker).setOnClickListener(v -> showDatePicker(false));
        findViewById(R.id.layoutReturnDatePicker).setOnClickListener(v -> showDatePicker(true));

        // Step 3
        tvConfirmLibrary = findViewById(R.id.tvConfirmLibrary);
        tvConfirmDate = findViewById(R.id.tvConfirmDate);
        tvConfirmTimeSlot = findViewById(R.id.tvConfirmTimeSlot);
        tvConfirmReturnDate = findViewById(R.id.tvConfirmReturnDate);
        tvRentalFee = findViewById(R.id.tvRentalFee);
        tvTotal = findViewById(R.id.tvTotal);
        tvDeposit = findViewById(R.id.tvDeposit);

        // RecyclerViews
        recyclerLibraries = findViewById(R.id.recyclerLibraries);
        recyclerTimeSlots = findViewById(R.id.recyclerTimeSlots);

        // Bottom Navigation
        NavigationHelper.setupBottomNavigation(this, R.id.nav_bookings);
    }

    private void setupRecyclerViews() {
        // Libraries
        libraryAdapter = new LibrarySelectAdapter();
        libraryAdapter.setOnLibrarySelectedListener((id, name) -> {
            selectedLibraryId = id;
            selectedLibraryName = name;
        });
        recyclerLibraries.setLayoutManager(new LinearLayoutManager(this));
        recyclerLibraries.setNestedScrollingEnabled(false);
        recyclerLibraries.setAdapter(libraryAdapter);

        // Time slots
        timeSlotAdapter = new TimeSlotAdapter(TIME_SLOTS);
        timeSlotAdapter.setOnTimeSlotSelectedListener((index, slot) -> {
            selectedTimeSlotIndex = index;
            selectedTimeSlot = slot;
        });
        recyclerTimeSlots.setLayoutManager(new LinearLayoutManager(this));
        recyclerTimeSlots.setNestedScrollingEnabled(false);
        recyclerTimeSlots.setAdapter(timeSlotAdapter);
    }

    private void setupClickListeners() {
        btnPrevious.setOnClickListener(v -> {
            if (currentStep > 1) {
                updateStep(currentStep - 1);
            }
        });

        btnContinue.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                if (currentStep < 3) {
                    updateStep(currentStep + 1);
                } else {
                    confirmReservation();
                }
            }
        });
    }

    private void loadBookInfo() {
        tvBookTitle.setText(bookTitle != null ? bookTitle : "Book");
        tvAuthor.setText(bookAuthor != null ? bookAuthor : "Author");

        if (bookCoverUrl != null && !bookCoverUrl.isEmpty()) {
            Glide.with(this)
                    .load(bookCoverUrl)
                    .placeholder(R.drawable.ic_library_book)
                    .into(ivBookCover);
        }

        // Display book price
        if (bookPrice > 0) {
            String priceFormatted = "₹" + String.format(Locale.getDefault(), "%.0f", bookPrice);
            tvDeposit.setText(priceFormatted);
            tvTotal.setText(priceFormatted);
        } else {
            tvDeposit.setText("₹0");
            tvTotal.setText("₹0");
        }
    }

    private void loadLibraries() {
        // Libraries with GPS coordinates
        List<LibrarySelectAdapter.LibraryItem> libraries = new ArrayList<>();
        // Central Delhi Public Library (Connaught Place)
        libraries.add(new LibrarySelectAdapter.LibraryItem(
                1, "Central Delhi Public Library",
                "Connaught Place, New Delhi, Delhi 110001",
                "Mon-Sat: 9:00 AM - 8:00 PM, Sun: 10:00 AM - 6:00 PM", 5,
                28.6315, 77.2167));
        // Mumbai Central Library (Fort)
        libraries.add(new LibrarySelectAdapter.LibraryItem(
                2, "Mumbai Central Library",
                "Fort, Mumbai, Maharashtra 400001",
                "Mon-Fri: 10:00 AM - 7:00 PM, Sat: 10:00 AM - 5:00 PM", 3,
                18.9322, 72.8333));
        // SIMATS Central Library (Chennai)
        libraries.add(new LibrarySelectAdapter.LibraryItem(
                3, "SIMATS Central Library",
                "Saveetha University, Chennai 602105",
                "Mon-Sun: 8:00 AM - 10:00 PM", 8,
                13.0540, 80.0184));
        // Anna Centenary Library (Chennai)
        libraries.add(new LibrarySelectAdapter.LibraryItem(
                4, "Anna Centenary Library",
                "Kotturpuram, Chennai 600025",
                "Mon-Sun: 9:00 AM - 8:00 PM", 6,
                13.0192, 80.2394));
        // Connemara Public Library (Chennai)
        libraries.add(new LibrarySelectAdapter.LibraryItem(
                5, "Connemara Public Library",
                "Pantheon Road, Egmore, Chennai 600008",
                "Mon-Sat: 9:30 AM - 7:00 PM", 4,
                13.0724, 80.2610));
        // Bangalore Central Library
        libraries.add(new LibrarySelectAdapter.LibraryItem(
                6, "Bangalore Central Library",
                "Cubbon Park, Bengaluru 560001",
                "Mon-Sat: 8:00 AM - 8:00 PM", 7,
                12.9752, 77.5912));

        // Get user location and sort by distance
        getUserLocationAndSortLibraries(libraries);
    }

    private void getUserLocationAndSortLibraries(List<LibrarySelectAdapter.LibraryItem> libraries) {
        // Check for location permission
        if (checkSelfPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Request permission
            requestPermissions(new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION }, 100);
            // Show libraries without sorting for now
            libraryAdapter.setLibraries(libraries);
            return;
        }

        try {
            android.location.LocationManager locationManager = (android.location.LocationManager) getSystemService(
                    LOCATION_SERVICE);
            android.location.Location lastLocation = locationManager
                    .getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);

            if (lastLocation == null) {
                lastLocation = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
            }

            if (lastLocation != null) {
                double userLat = lastLocation.getLatitude();
                double userLng = lastLocation.getLongitude();

                // Calculate distance for each library
                for (LibrarySelectAdapter.LibraryItem lib : libraries) {
                    lib.distanceKm = calculateDistance(userLat, userLng, lib.latitude, lib.longitude);
                    // Update address to show distance
                    lib.address = lib.address + " (📍 " + String.format("%.1f", lib.distanceKm) + " km)";
                }

                // Sort by distance (nearest first)
                java.util.Collections.sort(libraries, (a, b) -> Double.compare(a.distanceKm, b.distanceKm));

                Log.d(TAG, "User location: " + userLat + ", " + userLng);
                Log.d(TAG, "Libraries sorted by distance");
            } else {
                Log.d(TAG, "Could not get user location");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission error: " + e.getMessage());
        }

        libraryAdapter.setLibraries(libraries);
    }

    // Haversine formula to calculate distance between two GPS coordinates
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth's radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void updateStep(int step) {
        currentStep = step;

        // Update step indicators
        tvStep1.setBackgroundResource(step >= 1 ? R.drawable.bg_step_active : R.drawable.bg_step_inactive);
        tvStep1.setTextColor(getResources().getColor(step >= 1 ? android.R.color.white : android.R.color.darker_gray));

        tvStep2.setBackgroundResource(step >= 2 ? R.drawable.bg_step_active : R.drawable.bg_step_inactive);
        tvStep2.setTextColor(getResources().getColor(step >= 2 ? android.R.color.white : android.R.color.darker_gray));

        tvStep3.setBackgroundResource(step >= 3 ? R.drawable.bg_step_active : R.drawable.bg_step_inactive);
        tvStep3.setTextColor(getResources().getColor(step >= 3 ? android.R.color.white : android.R.color.darker_gray));

        // Show/hide layouts
        layoutStep1.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        layoutStep2.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        layoutStep3.setVisibility(step == 3 ? View.VISIBLE : View.GONE);

        // Update buttons
        btnPrevious.setVisibility(step > 1 ? View.VISIBLE : View.GONE);
        btnContinue.setText(step == 3 ? "Confirm Reservation" : "Continue");

        // Update step 3 confirmation details with deposit = book price
        if (step == 3) {
            tvConfirmLibrary.setText(selectedLibraryName);

            // Format dates as dd-MM-yyyy for display
            String displayDate = formatDateForDisplay(selectedDate);
            String displayReturnDate = formatDateForDisplay(selectedReturnDate);

            tvConfirmDate.setText(displayDate);
            tvConfirmTimeSlot.setText(selectedTimeSlot);
            tvConfirmReturnDate.setText(displayReturnDate);

            // Show book price as security deposit (refundable)
            String depositStr = bookPrice > 0 ? String.format("₹%.0f", bookPrice) : "₹0";
            tvDeposit.setText(depositStr);
            tvRentalFee.setText("FREE"); // Rental is free, only deposit
            tvTotal.setText(depositStr); // Total = deposit amount
        }
    }

    private boolean validateCurrentStep() {
        if (currentStep == 1) {
            if (selectedLibraryId == -1) {
                Toast.makeText(this, "Please select a library", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (currentStep == 2) {
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a pickup date", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (selectedReturnDate.isEmpty()) {
                Toast.makeText(this, "Please select a return date", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (selectedTimeSlotIndex == -1) {
                Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Check if booking for today and time slot has passed
            if (isTimeSlotPassed(selectedDate, selectedTimeSlotIndex)) {
                Toast.makeText(this, "This time slot has already passed. Please select a different time or date.",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the selected time slot has already passed for the given date
     */
    private boolean isTimeSlotPassed(String date, int slotIndex) {
        try {
            // Get today's date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = dateFormat.format(new java.util.Date());

            // If date is not today, time slot is valid
            if (!date.equals(today)) {
                return false;
            }

            // If booking for today, check if time slot has passed
            Calendar now = Calendar.getInstance();
            int currentHour = now.get(Calendar.HOUR_OF_DAY);

            // Parse the end time of the slot (e.g., "5:00 PM - 7:00 PM" -> 19)
            int[] slotEndHours = { 11, 13, 15, 17, 19 }; // 11 AM, 1 PM, 3 PM, 5 PM, 7 PM

            if (slotIndex >= 0 && slotIndex < slotEndHours.length) {
                int slotEndHour = slotEndHours[slotIndex];
                // If current time is past the slot end time, it's invalid
                if (currentHour >= slotEndHour) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Format date from yyyy-MM-dd to dd-MM-yyyy for display
     */
    private String formatDateForDisplay(String apiDate) {
        try {
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            java.util.Date date = apiFormat.parse(apiDate);
            return displayFormat.format(date);
        } catch (Exception e) {
            return apiDate; // Return original if parsing fails
        }
    }

    private void showDatePicker(boolean isReturnDate) {
        Calendar calendar = Calendar.getInstance();

        // For return date, start from the day after pickup date
        if (isReturnDate && !selectedDate.isEmpty()) {
            try {
                SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                java.util.Date pickupDate = apiFormat.parse(selectedDate);
                calendar.setTime(pickupDate);
                calendar.add(Calendar.DAY_OF_MONTH, 1); // Start from day after pickup
            } catch (Exception e) {
                // Use today if parsing fails
            }
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    // Format for display: "02/01/2026"
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    // Format for API: "yyyy-MM-dd"
                    SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    if (isReturnDate) {
                        selectedReturnDate = apiFormat.format(selected.getTime());
                        tvReturnDate.setText(displayFormat.format(selected.getTime()));
                        tvReturnDate.setTextColor(getResources().getColor(android.R.color.black));
                    } else {
                        selectedDate = apiFormat.format(selected.getTime());
                        tvSelectedDate.setText(displayFormat.format(selected.getTime()));
                        tvSelectedDate.setTextColor(getResources().getColor(android.R.color.black));
                        // Reset return date if pickup date changes
                        selectedReturnDate = "";
                        tvReturnDate.setText("dd/mm/yyyy");
                        tvReturnDate.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Set minimum date
        if (isReturnDate && !selectedDate.isEmpty()) {
            try {
                SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                java.util.Date pickupDate = apiFormat.parse(selectedDate);
                Calendar minDate = Calendar.getInstance();
                minDate.setTime(pickupDate);
                minDate.add(Calendar.DAY_OF_MONTH, 1);
                dialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            } catch (Exception e) {
                dialog.getDatePicker().setMinDate(System.currentTimeMillis());
            }
        } else {
            dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        }

        dialog.show();
    }

    private void confirmReservation() {
        // First process payment via Razorpay
        if (bookPrice > 0) {
            startRazorpayPayment();
        } else {
            // Free book - directly confirm
            processReservation();
        }
    }

    private void startRazorpayPayment() {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_RxlWPJvk2q2SPB"); // Razorpay Test Key

        try {
            JSONObject options = new JSONObject();
            options.put("name", "LibraryAI");
            options.put("description", "Book Deposit: " + bookTitle);
            options.put("currency", "INR");
            // Amount in paise (₹1 = 100 paise)
            int amountInPaise = (int) (bookPrice * 100);
            options.put("amount", amountInPaise);

            // Prefill user details
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            String userEmail = prefs.getString("email", "");
            String userName = prefs.getString("name", "");
            String userPhone = prefs.getString("phone", "");

            JSONObject prefill = new JSONObject();
            prefill.put("email", userEmail);
            prefill.put("contact", userPhone);
            options.put("prefill", prefill);

            // Theme color
            JSONObject theme = new JSONObject();
            theme.put("color", "#7C3AED");
            options.put("theme", theme);

            checkout.open(this, options);

        } catch (Exception e) {
            Log.e(TAG, "Razorpay Error: " + e.getMessage());
            Toast.makeText(this, "Payment error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentId) {
        // Payment successful - now confirm reservation
        Log.d(TAG, "Payment Success: " + razorpayPaymentId);
        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();
        processReservation();
    }

    @Override
    public void onPaymentError(int code, String response) {
        Log.e(TAG, "Payment Error: " + code + " - " + response);
        Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
    }

    private void processReservation() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        btnContinue.setEnabled(false);
        btnContinue.setText("Processing...");

        JSONObject data = new JSONObject();
        try {
            data.put("user_id", userId);
            data.put("book_id", bookId);
            data.put("library_id", selectedLibraryId);
            data.put("date", selectedDate);
            data.put("time_slot", selectedTimeSlot);
            data.put("deposit_amount", bookPrice);
            // Add due date (return date selected by user)
            if (selectedReturnDate != null && !selectedReturnDate.isEmpty()) {
                data.put("due_date", selectedReturnDate);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_CREATE_RESERVATION,
                data,
                response -> {
                    btnContinue.setEnabled(true);
                    btnContinue.setText("Confirm Reservation");
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "✅ Reservation confirmed!", Toast.LENGTH_SHORT).show();
                            // Go to My Bookings
                            Intent intent = new Intent(this, MyBookingsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, response.optString("message", "Reservation failed"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                    }
                },
                error -> {
                    btnContinue.setEnabled(true);
                    btnContinue.setText("Confirm Reservation");
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 1) {
            updateStep(currentStep - 1);
        } else {
            super.onBackPressed();
        }
    }

    // ========== Library Selection Adapter ==========
    public static class LibrarySelectAdapter extends RecyclerView.Adapter<LibrarySelectAdapter.ViewHolder> {
        private List<LibraryItem> libraries = new ArrayList<>();
        private int selectedPosition = -1;
        private OnLibrarySelectedListener listener;

        public static class LibraryItem {
            public int id;
            public String name, address, hours;
            public int available;
            public double latitude, longitude;
            public double distanceKm = 0; // Distance from user in km

            public LibraryItem(int id, String name, String address, String hours, int available, double lat,
                    double lng) {
                this.id = id;
                this.name = name;
                this.address = address;
                this.hours = hours;
                this.available = available;
                this.latitude = lat;
                this.longitude = lng;
            }
        }

        public interface OnLibrarySelectedListener {
            void onLibrarySelected(int id, String name);
        }

        public void setOnLibrarySelectedListener(OnLibrarySelectedListener listener) {
            this.listener = listener;
        }

        public void setLibraries(List<LibraryItem> libraries) {
            this.libraries = libraries;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_library_select, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LibraryItem item = libraries.get(position);
            holder.tvName.setText(item.name);
            holder.tvAddress.setText(item.address);
            holder.tvHours.setText(item.hours);
            holder.tvAvailable.setText("✓ " + item.available + " available");

            // Selection state
            boolean isSelected = selectedPosition == position;
            holder.ivSelected.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
            holder.itemView
                    .setBackgroundResource(isSelected ? R.drawable.bg_time_slot_selected : R.drawable.bg_card_border);

            holder.itemView.setOnClickListener(v -> {
                int prev = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(prev);
                notifyItemChanged(selectedPosition);
                if (listener != null) {
                    listener.onLibrarySelected(item.id, item.name);
                }
            });
        }

        @Override
        public int getItemCount() {
            return libraries.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvAddress, tvHours, tvAvailable;
            ImageView ivSelected;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvLibraryName);
                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvHours = itemView.findViewById(R.id.tvHours);
                tvAvailable = itemView.findViewById(R.id.tvAvailable);
                ivSelected = itemView.findViewById(R.id.ivSelected);
            }
        }
    }

    // ========== Time Slot Adapter ==========
    public static class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
        private final String[] timeSlots;
        private int selectedPosition = -1;
        private OnTimeSlotSelectedListener listener;

        public interface OnTimeSlotSelectedListener {
            void onTimeSlotSelected(int index, String slot);
        }

        public TimeSlotAdapter(String[] timeSlots) {
            this.timeSlots = timeSlots;
        }

        public void setOnTimeSlotSelectedListener(OnTimeSlotSelectedListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_time_slot, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String slot = timeSlots[position];
            holder.tvTimeSlot.setText(slot);

            boolean isSelected = selectedPosition == position;
            holder.tvTimeSlot.setBackgroundResource(
                    isSelected ? R.drawable.bg_time_slot_selected : R.drawable.bg_time_slot_unselected);
            holder.tvTimeSlot.setTextColor(holder.itemView.getContext().getResources()
                    .getColor(isSelected ? android.R.color.holo_blue_dark : android.R.color.darker_gray));

            // Add checkmark for selected
            holder.tvTimeSlot.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, isSelected ? R.drawable.ic_check_circle : 0, 0);

            holder.itemView.setOnClickListener(v -> {
                int prev = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(prev);
                notifyItemChanged(selectedPosition);
                if (listener != null) {
                    listener.onTimeSlotSelected(selectedPosition, slot);
                }
            });
        }

        @Override
        public int getItemCount() {
            return timeSlots.length;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTimeSlot;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTimeSlot = itemView.findViewById(R.id.tvTimeSlot);
            }
        }
    }
}
