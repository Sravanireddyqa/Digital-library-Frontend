package com.simats.digitallibrary;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Invoices Activity
 * Manages fines, penalties and payment records
 */
public class InvoicesActivity extends AppCompatActivity {

    private static final String TAG = "InvoicesActivity";

    private ImageButton btnBack;
    private EditText etSearch;
    private TextView chipAll, chipDeposits, chipRefunds, chipFines, chipPending;
    private RecyclerView recyclerInvoices;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private TextView tvTotalInvoices, tvTotalCollected, tvPendingAmount;

    private InvoiceAdapter adapter;
    private List<Invoice> allInvoices = new ArrayList<>();
    private String currentFilter = "all";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoices);

        initViews();
        setupRecyclerView();
        setupChips();
        setupSearch();
        loadInvoices();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        chipAll = findViewById(R.id.chipAll);
        chipDeposits = findViewById(R.id.chipDeposits);
        chipRefunds = findViewById(R.id.chipRefunds);
        chipFines = findViewById(R.id.chipFines);
        chipPending = findViewById(R.id.chipPending);
        recyclerInvoices = findViewById(R.id.recyclerInvoices);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvTotalInvoices = findViewById(R.id.tvTotalInvoices);
        tvTotalCollected = findViewById(R.id.tvTotalCollected);
        tvPendingAmount = findViewById(R.id.tvPendingAmount);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new InvoiceAdapter();
        adapter.setOnInvoiceActionListener(new InvoiceAdapter.OnInvoiceActionListener() {
            @Override
            public void onViewInvoice(Invoice invoice) {
                showInvoiceDetails(invoice);
            }

            @Override
            public void onMarkPaid(Invoice invoice) {
                confirmMarkPaid(invoice);
            }

            @Override
            public void onDownloadPdf(Invoice invoice) {
                Toast.makeText(InvoicesActivity.this,
                        "PDF download coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerInvoices.setLayoutManager(new LinearLayoutManager(this));
        recyclerInvoices.setNestedScrollingEnabled(true);
        recyclerInvoices.setAdapter(adapter);
    }

    private void setupChips() {
        View.OnClickListener chipListener = v -> {
            // Reset all chips
            resetChips();

            // Select clicked chip
            TextView chip = (TextView) v;
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(0xFFFFFFFF);

            if (v.getId() == R.id.chipAll)
                currentFilter = "all";
            else if (v.getId() == R.id.chipDeposits)
                currentFilter = "deposit";
            else if (v.getId() == R.id.chipRefunds)
                currentFilter = "refund";
            else if (v.getId() == R.id.chipFines)
                currentFilter = "fine";
            else if (v.getId() == R.id.chipPending)
                currentFilter = "pending";

            filterInvoices();
        };

        chipAll.setOnClickListener(chipListener);
        chipDeposits.setOnClickListener(chipListener);
        chipRefunds.setOnClickListener(chipListener);
        chipFines.setOnClickListener(chipListener);
        chipPending.setOnClickListener(chipListener);
    }

    private void resetChips() {
        chipAll.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipAll.setTextColor(0xFF6B7280);
        chipDeposits.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipDeposits.setTextColor(0xFF6B7280);
        chipRefunds.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipRefunds.setTextColor(0xFF6B7280);
        chipFines.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipFines.setTextColor(0xFF6B7280);
        chipPending.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipPending.setTextColor(0xFF6B7280);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase().trim();
                filterInvoices();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadInvoices() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_GET_INVOICES,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray invoicesArray = response.getJSONArray("invoices");
                            allInvoices.clear();

                            double totalCollected = 0;
                            double pendingAmount = 0;

                            for (int i = 0; i < invoicesArray.length(); i++) {
                                JSONObject obj = invoicesArray.getJSONObject(i);
                                Invoice invoice = new Invoice();
                                invoice.setId(obj.getInt("id"));
                                invoice.setInvoiceId(obj.optString("invoice_id", "INV-" + obj.getInt("id")));
                                invoice.setUserName(obj.optString("user_name", "Unknown"));
                                invoice.setUserEmail(obj.optString("user_email", ""));
                                invoice.setBookTitle(obj.optString("book_title", "Unknown Book"));
                                invoice.setReason(obj.optString("reason", "Fine"));
                                invoice.setAmount(obj.optDouble("amount", 0));
                                invoice.setStatus(obj.optString("status", "unpaid"));
                                invoice.setDate(obj.optString("date", ""));
                                allInvoices.add(invoice);

                                if ("paid".equalsIgnoreCase(invoice.getStatus())) {
                                    totalCollected += invoice.getAmount();
                                } else {
                                    pendingAmount += invoice.getAmount();
                                }
                            }

                            // Update summary
                            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                            tvTotalInvoices.setText(String.valueOf(allInvoices.size()));
                            tvTotalCollected.setText(format.format(totalCollected));
                            tvPendingAmount.setText(format.format(pendingAmount));

                            filterInvoices();
                        } else {
                            showEmpty();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                        showEmpty();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error: " + error.getMessage());
                    showEmpty();
                    Toast.makeText(this, "Failed to load invoices", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void filterInvoices() {
        List<Invoice> filtered = new ArrayList<>();

        for (Invoice invoice : allInvoices) {
            // Status filter
            if (!currentFilter.equals("all") &&
                    !invoice.getStatus().equalsIgnoreCase(currentFilter)) {
                continue;
            }

            // Search filter
            if (!searchQuery.isEmpty()) {
                String name = invoice.getUserName().toLowerCase();
                String book = invoice.getBookTitle().toLowerCase();
                String id = invoice.getInvoiceId().toLowerCase();
                if (!name.contains(searchQuery) &&
                        !book.contains(searchQuery) &&
                        !id.contains(searchQuery)) {
                    continue;
                }
            }

            filtered.add(invoice);
        }

        adapter.setInvoices(filtered);

        if (filtered.isEmpty()) {
            showEmpty();
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerInvoices.setVisibility(View.VISIBLE);
        }
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerInvoices.setVisibility(View.GONE);
    }

    private void showInvoiceDetails(Invoice invoice) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        String message = "Invoice: " + invoice.getInvoiceId() + "\n\n" +
                "User: " + invoice.getUserName() + "\n" +
                "Book: " + invoice.getBookTitle() + "\n" +
                "Reason: " + invoice.getReason() + "\n" +
                "Amount: " + format.format(invoice.getAmount()) + "\n" +
                "Status: " + invoice.getStatus() + "\n" +
                "Date: " + invoice.getDate();

        new AlertDialog.Builder(this)
                .setTitle("Invoice Details")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void confirmMarkPaid(Invoice invoice) {
        new AlertDialog.Builder(this)
                .setTitle("Mark as Paid")
                .setMessage("Mark invoice " + invoice.getInvoiceId() + " as paid?")
                .setPositiveButton("Yes", (d, w) -> markInvoicePaid(invoice))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markInvoicePaid(Invoice invoice) {
        JSONObject data = new JSONObject();
        try {
            data.put("invoice_id", invoice.getId());
            data.put("status", "paid");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.URL_UPDATE_INVOICE,
                data,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Invoice marked as paid", Toast.LENGTH_SHORT).show();
                            loadInvoices(); // Refresh
                        } else {
                            Toast.makeText(this,
                                    response.optString("message", "Update failed"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
