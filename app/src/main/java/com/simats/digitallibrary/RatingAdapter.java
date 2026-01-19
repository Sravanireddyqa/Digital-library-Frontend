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
 * Adapter for displaying ratings in admin view
 */
public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.RatingViewHolder> {

    private JSONArray ratings = new JSONArray();

    public void setRatings(JSONArray ratings) {
        this.ratings = ratings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rating, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        try {
            JSONObject rating = ratings.getJSONObject(position);
            holder.bind(rating);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return ratings.length();
    }

    static class RatingViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvBookTitle, tvUserName, tvRating, tvReview, tvDate;

        RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReview = itemView.findViewById(R.id.tvReview);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        void bind(JSONObject rating) {
            try {
                tvBookTitle.setText(rating.optString("book_title", "Unknown Book"));
                tvUserName.setText("by " + rating.optString("user_name", "User"));

                int stars = rating.optInt("rating", 0);
                StringBuilder starStr = new StringBuilder();
                for (int i = 0; i < stars; i++)
                    starStr.append("⭐");
                for (int i = stars; i < 5; i++)
                    starStr.append("☆");
                tvRating.setText(starStr.toString());

                String review = rating.optString("review", "");
                if (review.isEmpty()) {
                    tvReview.setVisibility(View.GONE);
                } else {
                    tvReview.setText("\"" + review + "\"");
                    tvReview.setVisibility(View.VISIBLE);
                }

                tvDate.setText(rating.optString("created_at", "").split(" ")[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
