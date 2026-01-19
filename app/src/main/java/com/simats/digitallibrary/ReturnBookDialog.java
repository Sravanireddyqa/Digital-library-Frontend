package com.simats.digitallibrary;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

/**
 * Dialog for selecting book return condition
 */
public class ReturnBookDialog extends Dialog {

    private Reservation reservation;
    private double depositAmount;
    private double bookPrice;
    private OnReturnConfirmedListener listener;

    // Views
    private TextView tvBookTitle, tvDepositAmount;
    private RadioGroup radioGroupCondition, radioGroupDamage;
    private RadioButton radioSafe, radioLate, radioDamaged, radioLost;
    private View layoutLateDays, layoutDamageLevel;
    private EditText etDaysLate, etNotes;
    private TextView tvFineAmount, tvRefundAmount;
    private TextView btnCancel, btnConfirm;

    // Fine rate per day
    private static final double FINE_PER_DAY = 10.0;

    public interface OnReturnConfirmedListener {
        void onReturnConfirmed(Reservation reservation, String condition, int daysLate, String damageLevel,
                String notes);
    }

    public ReturnBookDialog(@NonNull Context context, Reservation reservation, double depositAmount, double bookPrice) {
        super(context);
        this.reservation = reservation;
        this.depositAmount = depositAmount;
        this.bookPrice = bookPrice > 0 ? bookPrice : depositAmount;
    }

    public void setOnReturnConfirmedListener(OnReturnConfirmedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_return_book);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initViews();
        setupListeners();
        updateSummary();
    }

    private void initViews() {
        tvBookTitle = findViewById(R.id.tvBookTitle);
        tvDepositAmount = findViewById(R.id.tvDepositAmount);
        radioGroupCondition = findViewById(R.id.radioGroupCondition);
        radioGroupDamage = findViewById(R.id.radioGroupDamage);
        radioSafe = findViewById(R.id.radioSafe);
        radioLate = findViewById(R.id.radioLate);
        radioDamaged = findViewById(R.id.radioDamaged);
        radioLost = findViewById(R.id.radioLost);
        layoutLateDays = findViewById(R.id.layoutLateDays);
        layoutDamageLevel = findViewById(R.id.layoutDamageLevel);
        etDaysLate = findViewById(R.id.etDaysLate);
        etNotes = findViewById(R.id.etNotes);
        tvFineAmount = findViewById(R.id.tvFineAmount);
        tvRefundAmount = findViewById(R.id.tvRefundAmount);
        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Set book info
        tvBookTitle.setText(reservation.getBookTitle());
        tvDepositAmount.setText(String.format("Deposit: ₹%.0f", depositAmount));
    }

    private void setupListeners() {
        radioGroupCondition.setOnCheckedChangeListener((group, checkedId) -> {
            // Show/hide additional options based on selection
            layoutLateDays.setVisibility(checkedId == R.id.radioLate ? View.VISIBLE : View.GONE);
            layoutDamageLevel.setVisibility(checkedId == R.id.radioDamaged ? View.VISIBLE : View.GONE);
            updateSummary();
        });

        radioGroupDamage.setOnCheckedChangeListener((group, checkedId) -> updateSummary());

        etDaysLate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSummary();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                String condition = getSelectedCondition();
                int daysLate = 0;
                String damageLevel = "minor";
                String notes = etNotes.getText().toString().trim();

                if (condition.equals("late")) {
                    String daysStr = etDaysLate.getText().toString().trim();
                    if (daysStr.isEmpty()) {
                        Toast.makeText(getContext(), "Please enter days late", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    daysLate = Integer.parseInt(daysStr);
                    if (daysLate <= 0) {
                        Toast.makeText(getContext(), "Days late must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else if (condition.equals("damaged")) {
                    damageLevel = getSelectedDamageLevel();
                }

                listener.onReturnConfirmed(reservation, condition, daysLate, damageLevel, notes);
                dismiss();
            }
        });
    }

    private String getSelectedCondition() {
        int checkedId = radioGroupCondition.getCheckedRadioButtonId();
        if (checkedId == R.id.radioSafe)
            return "safe";
        if (checkedId == R.id.radioLate)
            return "late";
        if (checkedId == R.id.radioDamaged)
            return "damaged";
        if (checkedId == R.id.radioLost)
            return "lost";
        return "safe";
    }

    private String getSelectedDamageLevel() {
        int checkedId = radioGroupDamage.getCheckedRadioButtonId();
        if (checkedId == R.id.radioDamageMinor)
            return "minor";
        if (checkedId == R.id.radioDamageModerate)
            return "moderate";
        if (checkedId == R.id.radioDamageSevere)
            return "severe";
        return "minor";
    }

    private void updateSummary() {
        String condition = getSelectedCondition();
        double fineAmount = 0;
        double refundAmount = depositAmount;

        switch (condition) {
            case "safe":
                fineAmount = 0;
                refundAmount = depositAmount;
                break;

            case "late":
                String daysStr = etDaysLate.getText().toString().trim();
                int daysLate = daysStr.isEmpty() ? 0 : Integer.parseInt(daysStr);
                fineAmount = daysLate * FINE_PER_DAY;
                refundAmount = Math.max(0, depositAmount - fineAmount);
                break;

            case "damaged":
                String damageLevel = getSelectedDamageLevel();
                double damagePercent = 0.10; // default minor
                if (damageLevel.equals("moderate"))
                    damagePercent = 0.30;
                else if (damageLevel.equals("severe"))
                    damagePercent = 0.50;
                fineAmount = depositAmount * damagePercent;
                refundAmount = depositAmount - fineAmount;
                break;

            case "lost":
                fineAmount = bookPrice;
                if (bookPrice <= depositAmount) {
                    refundAmount = depositAmount - bookPrice;
                } else {
                    refundAmount = 0;
                }
                break;
        }

        tvFineAmount.setText(String.format("₹%.0f", fineAmount));
        tvRefundAmount.setText(String.format("₹%.0f", refundAmount));
    }
}
