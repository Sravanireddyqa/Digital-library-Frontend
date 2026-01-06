package com.simats.digitallibrary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity to view all book ratings for admin
 */
public class ViewRatingsActivity extends AppCompatActivity {

    private RecyclerView recyclerRatings;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvTotalRatings, tvAverageRating, tvAvgRating;
    private RatingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ratings);

        initViews();
        loadRatings();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerRatings = findViewById(R.id.recyclerRatings);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTotalRatings = findViewById(R.id.tvTotalRatings);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvAvgRating = findViewById(R.id.tvAvgRating);

        recyclerRatings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RatingAdapter();
        recyclerRatings.setAdapter(adapter);
    }

    private void loadRatings() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.URL_GET_RATINGS,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray ratingsArray = response.getJSONArray("ratings");
                            int totalRatings = response.optInt("total_ratings", 0);
                            double avgRating = response.optDouble("average_rating", 0);

                            tvTotalRatings.setText(String.valueOf(totalRatings));
                            tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f", avgRating));
                            tvAvgRating.setText("⭐ " + String.format(Locale.getDefault(), "%.1f", avgRating));

                            List<RatingItem> ratings = new ArrayList<>();
                            for (int i = 0; i < ratingsArray.length(); i++) {
                                JSONObject obj = ratingsArray.getJSONObject(i);
                                RatingItem item = new RatingItem();
                                item.bookTitle = obj.optString("book_title", "Unknown");
                                item.bookAuthor = obj.optString("book_author", "");
                                item.userName = obj.optString("user_name", "User");
                                item.userEmail = obj.optString("user_email", "");
                                item.rating = obj.optInt("rating", 0);
                                item.review = obj.optString("review", "");
                                item.createdAt = obj.optString("created_at", "");
                                ratings.add(item);
                            }

                            if (ratings.isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                            } else {
                                adapter.setRatings(ratings);
                            }
                        } else {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    // Rating item model
    static class RatingItem {
        String bookTitle;
        String bookAuthor;
        String userName;
        String userEmail;
        int rating;
        String review;
        String createdAt;
    }

    // Rating adapter
    class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.ViewHolder> {
        private List<RatingItem> ratings = new ArrayList<>();

        void setRatings(List<RatingItem> ratings) {
            this.ratings = ratings;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_rating, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RatingItem item = ratings.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return ratings.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvBookTitle, tvUserName, tvRating, tvReview, tvDate;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
                tvUserName = itemView.findViewById(R.id.tvUserName);
                tvRating = itemView.findViewById(R.id.tvRating);
                tvReview = itemView.findViewById(R.id.tvReview);
                tvDate = itemView.findViewById(R.id.tvDate);
            }

            void bind(RatingItem item) {
                tvBookTitle.setText(item.bookTitle);
                tvUserName.setText("by " + item.userName);

                // Build star rating
                StringBuilder stars = new StringBuilder();
                for (int i = 0; i < 5; i++) {
                    stars.append(i < item.rating ? "⭐" : "☆");
                }
                tvRating.setText(stars.toString());

                // Show review if available
                if (item.review != null && !item.review.isEmpty()) {
                    tvReview.setText("\"" + item.review + "\"");
                    tvReview.setVisibility(View.VISIBLE);
                } else {
                    tvReview.setVisibility(View.GONE);
                }

                // Format date
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    Date date = inputFormat.parse(item.createdAt);
                    if (date != null) {
                        tvDate.setText(outputFormat.format(date));
                    } else {
                        tvDate.setText(item.createdAt);
                    }
                } catch (ParseException e) {
                    tvDate.setText(item.createdAt);
                }
            }
        }
    }
}
