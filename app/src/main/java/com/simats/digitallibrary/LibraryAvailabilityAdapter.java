package com.simats.digitallibrary;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter for library availability in book details
 * Updated to match ReservationFlowActivity format with distance and hours
 */
public class LibraryAvailabilityAdapter extends RecyclerView.Adapter<LibraryAvailabilityAdapter.ViewHolder> {

    private List<LibraryAvailability> libraries = new ArrayList<>();
    private Context context;
    private double userLat = 0, userLng = 0;
    private boolean hasUserLocation = false;

    public static class LibraryAvailability {
        public int libraryId;
        public String name;
        public String address;
        public String hours;
        public int available;
        public double latitude;
        public double longitude;
        public double distanceKm = 0;

        // Updated constructor with GPS coordinates and hours
        public LibraryAvailability(int libraryId, String name, String address, String hours,
                int available, double latitude, double longitude) {
            this.libraryId = libraryId;
            this.name = name;
            this.address = address;
            this.hours = hours;
            this.available = available;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        // Legacy constructor for backward compatibility
        public LibraryAvailability(int libraryId, String name, String address, int available) {
            this.libraryId = libraryId;
            this.name = name;
            this.address = address;
            this.hours = "Mon-Sat: 9:00 AM - 6:00 PM";
            this.available = available;
            this.latitude = 0;
            this.longitude = 0;
        }
    }

    public void setLibraries(List<LibraryAvailability> libraries) {
        this.libraries = libraries;
        notifyDataSetChanged();
    }

    private void getUserLocation(Context context) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastLocation == null) {
                    lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

                if (lastLocation != null) {
                    userLat = lastLocation.getLatitude();
                    userLng = lastLocation.getLongitude();
                    hasUserLocation = true;

                    // Calculate distance for each library
                    for (LibraryAvailability lib : libraries) {
                        if (lib.latitude != 0 && lib.longitude != 0) {
                            lib.distanceKm = calculateDistance(userLat, userLng, lib.latitude, lib.longitude);
                        }
                    }

                    // Sort by distance (nearest first)
                    Collections.sort(libraries, (a, b) -> Double.compare(a.distanceKm, b.distanceKm));
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    // Haversine formula to calculate distance between two GPS coordinates
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth's radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        // Get user location on first create
        if (!hasUserLocation) {
            getUserLocation(context);
        }
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_library_availability, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LibraryAvailability library = libraries.get(position);
        holder.tvLibraryName.setText(library.name);

        // Show address with distance if available
        String addressText = library.address;
        if (library.distanceKm > 0) {
            addressText += " ( 📍 " + String.format("%.1f", library.distanceKm) + " km)";
        }
        holder.tvLibraryAddress.setText(addressText);

        // Show hours
        holder.tvHours.setText(library.hours);

        // Show availability badge
        holder.tvAvailableCount.setText("✓ " + library.available + " available");
    }

    @Override
    public int getItemCount() {
        return libraries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLibraryName, tvLibraryAddress, tvHours, tvAvailableCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLibraryName = itemView.findViewById(R.id.tvLibraryName);
            tvLibraryAddress = itemView.findViewById(R.id.tvLibraryAddress);
            tvHours = itemView.findViewById(R.id.tvHours);
            tvAvailableCount = itemView.findViewById(R.id.tvAvailableCount);
        }
    }
}
