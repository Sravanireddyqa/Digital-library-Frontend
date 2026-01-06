package com.simats.digitallibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying transactions in RecyclerView
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTransactionId;
        private final TextView tvType;
        private final TextView tvAmount;
        private final TextView tvUserName;
        private final TextView tvBookTitle;
        private final TextView tvReason;
        private final TextView tvDate;
        private final TextView tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionId = itemView.findViewById(R.id.tvTransactionId);
            tvType = itemView.findViewById(R.id.tvType);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(Transaction transaction) {
            tvTransactionId.setText(transaction.getTransactionId());
            tvType.setText(transaction.getTypeDisplay());
            tvType.setTextColor(transaction.getTypeColor());

            // Format amount with sign
            String amountStr;
            if (transaction.getType().equals("deposit") || transaction.getType().equals("fine")
                    || transaction.getType().equals("pending_fine")) {
                amountStr = String.format("+₹%.0f", transaction.getAmount());
                tvAmount.setTextColor(0xFF10B981); // Green for incoming
            } else {
                amountStr = String.format("-₹%.0f", transaction.getAmount());
                tvAmount.setTextColor(0xFFEF4444); // Red for outgoing
            }
            tvAmount.setText(amountStr);

            tvUserName.setText(transaction.getUserName());
            tvBookTitle.setText(transaction.getBookTitle());
            tvReason.setText(transaction.getReasonDisplay());

            // Format date
            String date = transaction.getCreatedAt();
            if (date != null && date.length() > 10) {
                date = date.substring(0, 10);
            }
            tvDate.setText(date);

            // Status badge
            String status = transaction.getStatus();
            if (status != null) {
                tvStatus.setText(status.toUpperCase());
                switch (status) {
                    case "completed":
                        tvStatus.setBackgroundResource(R.drawable.bg_status_approved);
                        tvStatus.setTextColor(0xFF10B981);
                        break;
                    case "pending":
                        tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                        tvStatus.setTextColor(0xFFF59E0B);
                        break;
                    default:
                        tvStatus.setBackgroundResource(R.drawable.bg_status_rejected);
                        tvStatus.setTextColor(0xFFEF4444);
                        break;
                }
            }
        }
    }
}
