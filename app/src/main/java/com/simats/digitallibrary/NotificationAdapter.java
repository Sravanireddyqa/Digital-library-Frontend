package com.simats.digitallibrary;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final Context context;
    private List<Notification> notifications;
    private OnNotificationClickListener clickListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);

        void onNotificationLongClick(Notification notification);
    }

    public NotificationAdapter(Context context, OnNotificationClickListener clickListener) {
        this.context = context;
        this.notifications = new ArrayList<>();
        this.clickListener = clickListener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    public void addNotification(Notification notification) {
        notifications.add(0, notification); // Add to top
        notifyItemInserted(0);
    }

    public void removeNotification(int position) {
        notifications.remove(position);
        notifyItemRemoved(position);
    }

    public void updateNotification(int position, Notification notification) {
        notifications.set(position, notification);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvIcon;
        private final TextView tvTitle;
        private final TextView tvMessage;
        private final TextView tvTime;
        private final View viewUnreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvNotificationIcon);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            viewUnreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
        }

        public void bind(Notification notification) {
            // Set icon based on notification type
            tvIcon.setText(notification.getType().getIcon());

            // Set title and message
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());

            // Set time
            tvTime.setText(notification.getTimeAgo());

            // Show/hide unread indicator and adjust styling
            if (notification.isRead()) {
                viewUnreadIndicator.setVisibility(View.GONE);
                tvTitle.setTypeface(null, Typeface.NORMAL);
                tvMessage.setAlpha(0.7f);
                itemView.setAlpha(0.8f);
            } else {
                viewUnreadIndicator.setVisibility(View.VISIBLE);
                tvTitle.setTypeface(null, Typeface.BOLD);
                tvMessage.setAlpha(1.0f);
                itemView.setAlpha(1.0f);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onNotificationClick(notification);
                }
            });

            // Long click listener
            itemView.setOnLongClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onNotificationLongClick(notification);
                }
                return true;
            });
        }
    }

    public int getUnreadCount() {
        int count = 0;
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                count++;
            }
        }
        return count;
    }

    public void markAllAsRead() {
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        notifyDataSetChanged();
    }

    public List<Notification> getNotifications() {
        return notifications;
    }
}
