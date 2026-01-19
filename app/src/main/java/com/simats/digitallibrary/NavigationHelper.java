package com.simats.digitallibrary;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Helper class to setup bottom navigation consistently across all activities
 */
public class NavigationHelper {

    public static void setupBottomNavigation(Activity activity, int selectedItemId) {
        BottomNavigationView bottomNav = activity.findViewById(R.id.bottomNavigation);
        if (bottomNav == null)
            return;

        // Set the selected item
        bottomNav.setSelectedItemId(selectedItemId);

        // Set the click listener
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Don't navigate if already on this page
            if (id == selectedItemId) {
                return true;
            }

            Intent intent = null;

            if (id == R.id.nav_home) {
                intent = new Intent(activity, ReaderDashboardActivity.class);
            } else if (id == R.id.nav_search) {
                intent = new Intent(activity, SearchBooksActivity.class);
            } else if (id == R.id.nav_libraries) {
                intent = new Intent(activity, BrowseLibrariesActivity.class);
            } else if (id == R.id.nav_bookings) {
                intent = new Intent(activity, MyBookingsActivity.class);
            } else if (id == R.id.nav_wishlist) {
                intent = new Intent(activity, WishlistActivity.class);
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0); // No animation for smooth transition
                return true;
            }

            return false;
        });
    }
}
