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
 * Adapter for displaying orders in RecyclerView
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders = new ArrayList<>();

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOrderId, tvOrderStatus, tvBookTitle;
        private final TextView tvUserName, tvUserEmail, tvOrderDate;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
        }

        public void bind(Order order) {
            tvOrderId.setText("Order #" + order.getId());
            tvBookTitle.setText(order.getBookTitle());
            tvUserName.setText(order.getUserName());
            tvUserEmail.setText("(" + order.getUserEmail() + ")");
            tvOrderDate.setText(order.getOrderDate());

            // Status badge
            String status = order.getStatus().toLowerCase();
            tvOrderStatus.setText(status);

            switch (status) {
                case "approved":
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_badge_green);
                    tvOrderStatus.setTextColor(0xFF10B981);
                    break;
                case "rejected":
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_badge_red);
                    tvOrderStatus.setTextColor(0xFFEF4444);
                    break;
                default: // pending
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_badge_yellow);
                    tvOrderStatus.setTextColor(0xFFF59E0B);
                    break;
            }
        }
    }
}
