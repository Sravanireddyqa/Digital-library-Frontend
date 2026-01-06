package com.simats.digitallibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Adapter for Recent Activity items on Admin Dashboard
 */
public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private JSONArray activities = new JSONArray();

    public void setActivities(JSONArray activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject activity = activities.getJSONObject(position);

            holder.tvTitle.setText(activity.optString("title", "Activity"));
            holder.tvDescription.setText(activity.optString("description", ""));
            holder.tvTime.setText(activity.optString("time_ago", ""));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return activities.length();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvActivityTitle);
            tvDescription = itemView.findViewById(R.id.tvActivityDescription);
            tvTime = itemView.findViewById(R.id.tvActivityTime);
        }
    }
}
