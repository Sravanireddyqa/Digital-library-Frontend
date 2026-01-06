package com.simats.digitallibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

/**
 * Onboarding Activity that displays feature slides for first-time users
 */
public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "OnboardingPrefs";
    private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";

    private ViewPager2 viewPager;
    private LinearLayout layoutIndicators;
    private TextView btnNext;
    private TextView tvSkip;
    private OnboardingAdapter onboardingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in - go directly to dashboard
        SharedPreferences userPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        if (userPrefs.getBoolean("isLoggedIn", false)) {
            String role = userPrefs.getString("userRole", "reader");
            Intent intent;
            if ("admin".equalsIgnoreCase(role)) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else {
                intent = new Intent(this, ReaderDashboardActivity.class);
            }
            startActivity(intent);
            finish();
            return;
        }

        // If onboarding already completed, skip to role selection (login flow)
        if (isOnboardingCompleted()) {
            navigateToSelectAccountType();
            return;
        }

        // Show onboarding for first-time users
        setContentView(R.layout.activity_onboarding);

        initViews();
        setupOnboardingItems();
        setupIndicators();
        setCurrentIndicator(0);
        setupListeners();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        layoutIndicators = findViewById(R.id.layoutIndicators);
        btnNext = findViewById(R.id.btnNext);
        tvSkip = findViewById(R.id.tvSkip);
    }

    private void setupOnboardingItems() {
        List<OnboardingItem> onboardingItems = new ArrayList<>();

        // Slide 1: AI-Powered Semantic Search
        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_search_feature,
                "AI-Powered Semantic Search",
                "Discover books using natural language and advanced NLP. Just describe what you want to read, and our AI will understand your intent and find the perfect match."));

        // Slide 2: Find Nearby Libraries
        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_location_feature,
                "Find Nearby Libraries",
                "Locate libraries near you with interactive maps. Check real-time availability, read ratings and reviews, and get directions to your nearest location."));

        // Slide 3: Smart Reservations & Tracking
        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_reservations_feature,
                "Smart Reservations & Tracking",
                "Reserve books instantly, track due dates, and manage your reading schedule. Get automatic reminders and never miss a return date."));

        // Slide 4: Wishlist & Recommendations
        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_wishlist_feature,
                "Wishlist & Recommendations",
                "Save books to your wishlist and get personalized recommendations. Our AI suggests similar books based on your interests and reading history."));

        onboardingAdapter = new OnboardingAdapter(onboardingItems);
        viewPager.setAdapter(onboardingAdapter);
    }

    private void setupIndicators() {
        ImageView[] indicators = new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.indicator_inactive));
            indicators[i].setLayoutParams(layoutParams);
            layoutIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentIndicator(int index) {
        int childCount = layoutIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutIndicators.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(),
                        R.drawable.indicator_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(),
                        R.drawable.indicator_inactive));
            }
        }
    }

    private void setupListeners() {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);

                // Update button text on last page
                if (position == onboardingAdapter.getItemCount() - 1) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                completeOnboarding();
            }
        });

        tvSkip.setOnClickListener(v -> completeOnboarding());
    }

    private void completeOnboarding() {
        // Save that onboarding is complete
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply();

        navigateToSelectAccountType();
    }

    private boolean isOnboardingCompleted() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false);
    }

    private void navigateToSelectAccountType() {
        // Go directly to Login - role will be determined from database after login
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
