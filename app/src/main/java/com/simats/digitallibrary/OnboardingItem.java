package com.simats.digitallibrary;

/**
 * Data class representing a single onboarding slide
 */
public class OnboardingItem {
    private int featureIcon;
    private String featureTitle;
    private String featureDescription;

    public OnboardingItem(int featureIcon, String featureTitle, String featureDescription) {
        this.featureIcon = featureIcon;
        this.featureTitle = featureTitle;
        this.featureDescription = featureDescription;
    }

    public int getFeatureIcon() {
        return featureIcon;
    }

    public void setFeatureIcon(int featureIcon) {
        this.featureIcon = featureIcon;
    }

    public String getFeatureTitle() {
        return featureTitle;
    }

    public void setFeatureTitle(String featureTitle) {
        this.featureTitle = featureTitle;
    }

    public String getFeatureDescription() {
        return featureDescription;
    }

    public void setFeatureDescription(String featureDescription) {
        this.featureDescription = featureDescription;
    }
}
