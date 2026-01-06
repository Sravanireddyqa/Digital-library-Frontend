package com.simats.digitallibrary;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Dialog Fragment for Editing a Book
 */
public class EditBookDialogFragment extends DialogFragment {

    private EditText etBookTitle, etAuthor, etISBN, etPrice, etStock;
    private EditText etPublisher, etPublishedDate, etPages, etDescription;
    private Spinner spinnerCategory;
    private Button btnCancel, btnSaveBook, btnSelectCoverImage;
    private ImageButton btnClose;
    private ImageView ivBookCover;
    private TextView tvImageFileName;

    private Book book;
    private OnBookUpdatedListener listener;

    // Image upload variables
    private Uri selectedImageUri = null;
    private String uploadedCoverUrl = null;
    private boolean hasNewImage = false;
    private ActivityResultLauncher<String> imagePickerLauncher;

    private static final String[] CATEGORIES = {
            "Select Category",
            "Fiction",
            "Non-Fiction",
            "Mystery",
            "Science Fiction",
            "Romance",
            "Horror",
            "Biography",
            "History",
            "Science",
            "Self-Help"
    };

    public interface OnBookUpdatedListener {
        void onBookUpdated();
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public void setOnBookUpdatedListener(OnBookUpdatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Digitallibrary2_Dialog);

        // Initialize image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        onImageSelected(uri);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_book, container, false);

        initViews(view);
        setupCategorySpinner();
        populateFields();
        setupListeners();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void initViews(View view) {
        etBookTitle = view.findViewById(R.id.etBookTitle);
        etAuthor = view.findViewById(R.id.etAuthor);
        etISBN = view.findViewById(R.id.etISBN);
        etPrice = view.findViewById(R.id.etPrice);
        etStock = view.findViewById(R.id.etStock);
        etPublisher = view.findViewById(R.id.etPublisher);
        etPublishedDate = view.findViewById(R.id.etPublishedDate);
        etPages = view.findViewById(R.id.etPages);
        etDescription = view.findViewById(R.id.etDescription);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSaveBook = view.findViewById(R.id.btnSaveBook);
        btnClose = view.findViewById(R.id.btnClose);

        // Change button text to "Update"
        btnSaveBook.setText("Update Book");

        // Image upload views
        ivBookCover = view.findViewById(R.id.ivBookCover);
        btnSelectCoverImage = view.findViewById(R.id.btnSelectCoverImage);
        tvImageFileName = view.findViewById(R.id.tvImageFileName);
        btnSelectCoverImage.setText("Change Cover Image");
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                CATEGORIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void populateFields() {
        if (book == null)
            return;

        etBookTitle.setText(book.getTitle());
        etAuthor.setText(book.getAuthor());
        etISBN.setText(book.getIsbn());
        etPrice.setText(String.valueOf(book.getPrice()));
        etStock.setText(String.valueOf(book.getStock()));
        etPublisher.setText(book.getPublisher());
        etPublishedDate.setText(book.getPublishedDate());
        etPages.setText(String.valueOf(book.getPages()));
        etDescription.setText(book.getDescription());

        // Set category spinner
        String category = book.getCategory();
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equalsIgnoreCase(category)) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        // Load existing cover image if available
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            String coverUrl = book.getCoverUrl();
            String imageUrl;
            // Handle relative paths like "uploads/..."
            if (coverUrl.startsWith("http")) {
                imageUrl = coverUrl;
            } else {
                imageUrl = ApiConfig.BASE_URL + coverUrl;
            }
            Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .into(ivBookCover);
            tvImageFileName.setText("Current cover image");
            tvImageFileName.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());
        btnSaveBook.setOnClickListener(v -> updateBook());
        etPublishedDate.setOnClickListener(v -> showDatePicker());

        // Image picker
        btnSelectCoverImage.setOnClickListener(v -> selectCoverImage());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    etPublishedDate.setText(date);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void updateBook() {
        String title = etBookTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String isbn = etISBN.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String publisher = etPublisher.getText().toString().trim();
        String publishedDate = etPublishedDate.getText().toString().trim();
        String pagesStr = etPages.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        int categoryIndex = spinnerCategory.getSelectedItemPosition();

        if (title.isEmpty()) {
            etBookTitle.setError("Title required");
            return;
        }

        String category = categoryIndex > 0 ? CATEGORIES[categoryIndex] : book.getCategory();

        final double price;
        final int stock;
        final int pages;

        try {
            if (!priceStr.isEmpty()) {
                price = Double.parseDouble(priceStr.replace("$", ""));
            } else {
                price = 0;
            }
            if (!stockStr.isEmpty()) {
                stock = Integer.parseInt(stockStr);
            } else {
                stock = 0;
            }
            if (!pagesStr.isEmpty()) {
                pages = Integer.parseInt(pagesStr);
            } else {
                pages = 0;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Handle image upload if new image selected
        if (hasNewImage && selectedImageUri != null) {
            // Upload new image first, then delete old one and update book
            uploadCoverImage(newCoverUrl -> {
                // Delete old image if exists and upload was successful
                if (newCoverUrl != null && book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
                    deleteOldCoverImage(book.getCoverUrl());
                }
                saveBookUpdate(title, author, isbn, category, price, stock, publisher, publishedDate, pages,
                        description, newCoverUrl);
            });
        } else {
            // No new image, just update with existing cover_url
            saveBookUpdate(title, author, isbn, category, price, stock, publisher, publishedDate, pages, description,
                    book.getCoverUrl());
        }
    }

    /**
     * Save book update to server
     */
    private void saveBookUpdate(String title, String author, String isbn, String category,
            double price, int stock, String publisher, String publishedDate,
            int pages, String description, String coverUrl) {
        JSONObject bookData = new JSONObject();
        try {
            bookData.put("book_id", book.getId());
            bookData.put("title", title);
            bookData.put("author", author);
            bookData.put("isbn", isbn);
            bookData.put("category", category);
            bookData.put("price", price);
            bookData.put("stock", stock);
            bookData.put("publisher", publisher);
            bookData.put("published_date", publishedDate);
            bookData.put("pages", pages);
            bookData.put("description", description);

            // Add cover_url if exists
            if (coverUrl != null && !coverUrl.isEmpty()) {
                bookData.put("cover_url", coverUrl);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        btnSaveBook.setEnabled(false);
        btnSaveBook.setText("Updating...");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_UPDATE_BOOK,
                bookData,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(getContext(), "Book updated!", Toast.LENGTH_SHORT).show();
                            if (listener != null)
                                listener.onBookUpdated();
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), response.optString("message", "Update failed"),
                                    Toast.LENGTH_SHORT).show();
                            btnSaveBook.setEnabled(true);
                            btnSaveBook.setText("Update Book");
                        }
                    } catch (JSONException e) {
                        btnSaveBook.setEnabled(true);
                        btnSaveBook.setText("Update Book");
                    }
                },
                error -> {
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    btnSaveBook.setEnabled(true);
                    btnSaveBook.setText("Update Book");
                });

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    /**
     * Launch image picker
     */
    private void selectCoverImage() {
        imagePickerLauncher.launch("image/*");
    }

    /**
     * Handle selected image
     */
    private void onImageSelected(Uri uri) {
        selectedImageUri = uri;
        hasNewImage = true;

        // Validate image format
        if (!ImageUploadHelper.isValidImageFormat(requireContext(), uri)) {
            Toast.makeText(getContext(), "Invalid image format. Only JPG and PNG allowed.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate file size
        if (!ImageUploadHelper.isValidFileSize(requireContext(), uri)) {
            Toast.makeText(getContext(), "Image too large. Maximum 5MB allowed.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Display image preview
        Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(ivBookCover);

        // Show file name
        tvImageFileName.setText("New image selected");
        tvImageFileName.setVisibility(View.VISIBLE);
    }

    /**
     * Upload cover image to server
     */
    private void uploadCoverImage(UploadCallback callback) {
        if (selectedImageUri == null) {
            callback.onComplete(null);
            return;
        }

        btnSaveBook.setEnabled(false);
        btnSaveBook.setText("Uploading image...");

        try {
            // Compress image
            byte[] imageBytes = ImageUploadHelper.compressImage(requireContext(), selectedImageUri);
            String fileExtension = ImageUploadHelper.getFileExtension(requireContext(), selectedImageUri);
            String fileName = "book_cover_" + System.currentTimeMillis() + "." + fileExtension;

            // Create multipart request
            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                    Request.Method.POST,
                    ApiConfig.URL_UPLOAD_IMAGE,
                    response -> {
                        try {
                            String jsonString = new String(response.data, "UTF-8");
                            JSONObject jsonResponse = new JSONObject(jsonString);

                            if (jsonResponse.getBoolean("success")) {
                                uploadedCoverUrl = jsonResponse.getString("cover_url");
                                callback.onComplete(uploadedCoverUrl);
                            } else {
                                String message = jsonResponse.optString("message", "Upload failed");
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                callback.onComplete(null);
                            }
                        } catch (JSONException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Failed to parse upload response", Toast.LENGTH_SHORT).show();
                            callback.onComplete(null);
                        }
                    },
                    error -> {
                        Toast.makeText(getContext(), "Image upload failed. Please try again.", Toast.LENGTH_SHORT)
                                .show();
                        callback.onComplete(null);
                    });

            // Add image data
            HashMap<String, VolleyMultipartRequest.DataPart> byteData = new HashMap<>();
            byteData.put("image", new VolleyMultipartRequest.DataPart(
                    fileName,
                    imageBytes,
                    "image/" + fileExtension));
            multipartRequest.setByteData(byteData);

            // Add to request queue
            VolleySingleton.getInstance(requireContext()).addToRequestQueue(multipartRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to process image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            callback.onComplete(null);
        }
    }

    /**
     * Delete old cover image
     */
    private void deleteOldCoverImage(String coverUrl) {
        JSONObject data = new JSONObject();
        try {
            data.put("cover_url", coverUrl);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_DELETE_IMAGE,
                data,
                response -> {
                    // Image deleted - no action needed
                },
                error -> {
                    // Deletion failed - log but don't interrupt flow
                });

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    /**
     * Callback interface for upload completion
     */
    private interface UploadCallback {
        void onComplete(String coverUrl);
    }
}
