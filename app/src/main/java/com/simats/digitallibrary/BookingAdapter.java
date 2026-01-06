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
 * Adapter for user bookings
 */
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookings = new ArrayList<>();
    private OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onCancelBooking(Booking booking);
    }

    public void setOnBookingActionListener(OnBookingActionListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStatus, tvDate, tvBookTitle, tvAuthor, tvPickupDate, btnCancel;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvPickupDate = itemView.findViewById(R.id.tvPickupDate);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }

        public void bind(Booking booking) {
            tvBookTitle.setText(booking.getBookTitle());
            tvAuthor.setText(booking.getAuthor());
            tvDate.setText(booking.getCreatedAt());
            tvPickupDate.setText(booking.getPickupDate());

            // Status badge
            String status = booking.getStatus().toLowerCase();
            tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));

            switch (status) {
                case "approved":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_green);
                    tvStatus.setTextColor(0xFF10B981);
                    break;
                case "rejected":
                case "cancelled":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_red);
                    tvStatus.setTextColor(0xFFEF4444);
                    btnCancel.setVisibility(View.GONE);
                    break;
                default: // pending
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_yellow);
                    tvStatus.setTextColor(0xFFF59E0B);
                    btnCancel.setVisibility(View.VISIBLE);
                    break;
            }

            btnCancel.setOnClickListener(v -> {
                if (listener != null)
                    listener.onCancelBooking(booking);
            });
        }
    }
}
