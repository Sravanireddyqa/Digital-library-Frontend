package com.simats.digitallibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for displaying onboarding slides in ViewPager2
 */
public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private List<OnboardingItem> onboardingItems;

    public OnboardingAdapter(List<OnboardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding_slide, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.bind(onboardingItems.get(position));
    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivFeatureIcon;
        private TextView tvFeatureTitle;
        private TextView tvFeatureDescription;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFeatureIcon = itemView.findViewById(R.id.ivFeatureIcon);
            tvFeatureTitle = itemView.findViewById(R.id.tvFeatureTitle);
            tvFeatureDescription = itemView.findViewById(R.id.tvFeatureDescription);
        }

        public void bind(OnboardingItem item) {
            ivFeatureIcon.setImageResource(item.getFeatureIcon());
            tvFeatureTitle.setText(item.getFeatureTitle());
            tvFeatureDescription.setText(item.getFeatureDescription());
        }
    }
}
