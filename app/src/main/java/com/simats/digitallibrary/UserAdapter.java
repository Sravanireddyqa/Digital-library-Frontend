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
 * Adapter for displaying users in RecyclerView
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users = new ArrayList<>();
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onBlockUser(User user);

        void onUnblockUser(User user);
    }

    public void setOnUserActionListener(OnUserActionListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserName, tvUserEmail, tvUserType, tvUserStatus, btnAction;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserType = itemView.findViewById(R.id.tvUserType);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
            btnAction = itemView.findViewById(R.id.btnAction);
        }

        public void bind(User user) {
            tvUserName.setText(user.getName());
            tvUserEmail.setText(user.getEmail());

            // Type badge
            String type = user.getType();
            tvUserType.setText(type);
            if ("admin".equalsIgnoreCase(type)) {
                tvUserType.setBackgroundResource(R.drawable.bg_badge_purple);
                tvUserType.setTextColor(0xFF7C3AED);
            } else {
                tvUserType.setBackgroundResource(R.drawable.bg_badge_blue);
                tvUserType.setTextColor(0xFF3B82F6);
            }

            // Status badge
            String status = user.getStatus();
            tvUserStatus.setText(status);
            if ("blocked".equalsIgnoreCase(status)) {
                tvUserStatus.setBackgroundResource(R.drawable.bg_badge_red);
                tvUserStatus.setTextColor(0xFFEF4444);
                btnAction.setText("Unblock");
                btnAction.setTextColor(0xFF10B981);
            } else {
                tvUserStatus.setBackgroundResource(R.drawable.bg_badge_green);
                tvUserStatus.setTextColor(0xFF10B981);
                btnAction.setText("Block");
                btnAction.setTextColor(0xFFEF4444);
            }

            // Action button
            btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    if ("blocked".equalsIgnoreCase(status)) {
                        listener.onUnblockUser(user);
                    } else {
                        listener.onBlockUser(user);
                    }
                }
            });
        }
    }
}
