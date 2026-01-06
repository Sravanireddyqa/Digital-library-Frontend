package com.simats.digitallibrary;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Ratings Activity - Admin view of all user ratings
 */
public class RatingsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView recyclerRatings;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvStats;
    private RatingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);

        initViews();
        setupRecyclerView();
        loadRatings();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        recyclerRatings = findViewById(R.id.recyclerRatings);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvStats = findViewById(R.id.tvStats);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new RatingAdapter();
        recyclerRatings.setLayoutManager(new LinearLayoutManager(this));
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
                            int count = response.optInt("count", 0);
                            double avgRating = response.optDouble("average_rating", 0);

                            tvStats.setText(String.format("Total: %d ratings | Average: %.1f ⭐", count, avgRating));

                            if (ratingsArray.length() > 0) {
                                adapter.setRatings(ratingsArray);
                                recyclerRatings.setVisibility(View.VISIBLE);
                            } else {
                                tvEmpty.setVisibility(View.VISIBLE);
                                recyclerRatings.setVisibility(View.GONE);
                            }
                        } else {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Failed to load ratings", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
