package com.simats.digitallibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying invoices in RecyclerView
 */
public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    private List<Invoice> invoices = new ArrayList<>();
    private OnInvoiceActionListener listener;

    public interface OnInvoiceActionListener {
        void onViewInvoice(Invoice invoice);

        void onMarkPaid(Invoice invoice);

        void onDownloadPdf(Invoice invoice);
    }

    public void setOnInvoiceActionListener(OnInvoiceActionListener listener) {
        this.listener = listener;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invoice, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        Invoice invoice = invoices.get(position);
        holder.bind(invoice);
    }

    @Override
    public int getItemCount() {
        return invoices.size();
    }

    class InvoiceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvInvoiceId, tvStatus, tvUserName, tvBookTitle;
        private final TextView tvReason, tvAmount, tvDate;
        private final TextView btnViewInvoice, btnMarkPaid, btnDownload;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceId = itemView.findViewById(R.id.tvInvoiceId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnViewInvoice = itemView.findViewById(R.id.btnViewInvoice);
            btnMarkPaid = itemView.findViewById(R.id.btnMarkPaid);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }

        public void bind(Invoice invoice) {
            tvInvoiceId.setText(invoice.getInvoiceId());
            tvUserName.setText(invoice.getUserName());
            tvBookTitle.setText(invoice.getBookTitle());
            tvReason.setText(invoice.getReason());
            tvDate.setText(invoice.getDate());

            // Format amount
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            tvAmount.setText(format.format(invoice.getAmount()));

            // Status badge
            String status = invoice.getStatus().toLowerCase();
            tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));

            switch (status) {
                case "paid":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_green);
                    tvStatus.setTextColor(0xFF10B981);
                    btnMarkPaid.setVisibility(View.GONE);
                    break;
                case "overdue":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_red);
                    tvStatus.setTextColor(0xFFEF4444);
                    btnMarkPaid.setVisibility(View.VISIBLE);
                    break;
                default: // unpaid
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_yellow);
                    tvStatus.setTextColor(0xFFF59E0B);
                    btnMarkPaid.setVisibility(View.VISIBLE);
                    break;
            }

            // Action buttons
            btnViewInvoice.setOnClickListener(v -> {
                if (listener != null)
                    listener.onViewInvoice(invoice);
            });

            btnMarkPaid.setOnClickListener(v -> {
                if (listener != null)
                    listener.onMarkPaid(invoice);
            });

            btnDownload.setOnClickListener(v -> {
                if (listener != null)
                    listener.onDownloadPdf(invoice);
            });
        }
    }
}
