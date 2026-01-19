package com.simats.digitallibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Reservation Adapter for RecyclerView
 */
public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private List<Reservation> reservations = new ArrayList<>();
    private OnReservationActionListener listener;

    public interface OnReservationActionListener {
        void onApprove(Reservation reservation);

        void onReject(Reservation reservation);

        void onReturn(Reservation reservation);

        void onSendReminder(Reservation reservation);
    }

    public void setOnReservationActionListener(OnReservationActionListener listener) {
        this.listener = listener;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
        notifyDataSetChanged();
    }

    public void updateReservation(Reservation reservation, String newStatus) {
        int position = reservations.indexOf(reservation);
        if (position != -1) {
            reservation.setStatus(newStatus);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation_admin, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        holder.bind(reservation);
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    class ReservationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvBookTitle;
        private final TextView tvStatus;
        private final TextView tvReservationId;
        private final TextView tvUserName;
        private final TextView tvUserEmail;
        private final TextView tvLibrary;
        private final TextView tvPickupDate;
        private final TextView tvPickupTime;
        private final TextView tvRequestedAt;
        private final TextView tvCancelReason;
        private final LinearLayout layoutActions;
        private final Button btnApprove;
        private final Button btnReject;

        ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvReservationId = itemView.findViewById(R.id.tvReservationId);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvLibrary = itemView.findViewById(R.id.tvLibrary);
            tvPickupDate = itemView.findViewById(R.id.tvPickupDate);
            tvPickupTime = itemView.findViewById(R.id.tvPickupTime);
            tvRequestedAt = itemView.findViewById(R.id.tvRequestedAt);
            tvCancelReason = itemView.findViewById(R.id.tvCancelReason);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        void bind(Reservation reservation) {
            tvBookTitle.setText(reservation.getBookTitle());
            tvReservationId.setText("Reservation ID: " + reservation.getReservationId());
            tvUserName.setText(reservation.getUserName());
            tvUserEmail.setText(reservation.getUserEmail());
            tvLibrary.setText(reservation.getLibrary() != null ? reservation.getLibrary() : "Library");
            tvPickupDate.setText(reservation.getPickupDate());
            tvPickupTime.setText(reservation.getPickupTime() != null ? reservation.getPickupTime() : "");
            tvRequestedAt.setText(reservation.getRequestedAt());

            // Set status appearance
            String status = reservation.getStatus();
            if (status == null)
                status = "pending";

            switch (status.toLowerCase()) {
                case "approved":
                    tvStatus.setText("APPROVED");
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                    tvStatus.setBackgroundResource(R.drawable.bg_status_approved);
                    // Show Return and Remind buttons for approved reservations
                    layoutActions.setVisibility(View.VISIBLE);
                    btnApprove.setText("Return");
                    btnApprove.setBackgroundResource(R.drawable.bg_button_purple);
                    btnReject.setText("Remind");
                    btnReject.setVisibility(View.VISIBLE);
                    btnReject.setBackgroundResource(R.drawable.bg_button_outline);
                    btnReject.setTextColor(0xFF6B21A8); // Purple text to be visible
                    tvCancelReason.setVisibility(View.GONE);
                    break;
                case "rejected":
                    tvStatus.setText("REJECTED");
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                    tvStatus.setBackgroundResource(R.drawable.bg_status_rejected);
                    layoutActions.setVisibility(View.GONE);
                    tvCancelReason.setVisibility(View.GONE);
                    break;
                case "returned":
                    tvStatus.setText("RETURNED");
                    tvStatus.setTextColor(0xFF3B82F6); // Blue color
                    tvStatus.setBackgroundResource(R.drawable.bg_status_approved);
                    layoutActions.setVisibility(View.GONE);
                    tvCancelReason.setVisibility(View.GONE);
                    break;
                case "cancelled":
                    tvStatus.setText("CANCELLED");
                    tvStatus.setTextColor(0xFF6B7280); // Gray color
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                    layoutActions.setVisibility(View.GONE);
                    // Show cancel reason if available
                    String cancelReason = reservation.getCancelReason();
                    if (cancelReason != null && !cancelReason.isEmpty()) {
                        tvCancelReason.setText("📝 Reason: " + cancelReason);
                        tvCancelReason.setVisibility(View.VISIBLE);
                    } else {
                        tvCancelReason.setVisibility(View.GONE);
                    }
                    break;
                default: // pending
                    tvStatus.setText("PENDING");
                    tvStatus.setTextColor(0xFFF59E0B);
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                    layoutActions.setVisibility(View.VISIBLE);
                    btnApprove.setText("Approve");
                    btnReject.setVisibility(View.VISIBLE);
                    tvCancelReason.setVisibility(View.GONE);
                    break;
            }

            // Need final variable for lambda
            final String currentStatus = reservation.getStatus() != null ? reservation.getStatus().toLowerCase()
                    : "pending";

            btnApprove.setOnClickListener(v -> {
                if (listener != null) {
                    if (currentStatus.equals("approved")) {
                        listener.onReturn(reservation);
                    } else {
                        listener.onApprove(reservation);
                    }
                }
            });

            btnReject.setOnClickListener(v -> {
                if (listener != null) {
                    if (currentStatus.equals("approved")) {
                        listener.onSendReminder(reservation);
                    } else {
                        listener.onReject(reservation);
                    }
                }
            });
        }
    }
}
