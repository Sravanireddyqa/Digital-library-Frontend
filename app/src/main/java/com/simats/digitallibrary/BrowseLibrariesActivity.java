package com.simats.digitallibrary;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Browse Libraries Activity - Shows nearby libraries
 */
public class BrowseLibrariesActivity extends AppCompatActivity {

    private static final String TAG = "NearbyLibraries";
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private RecyclerView recyclerLibraries;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private TextView tvLibrariesCount, tvCurrentLocation;

    private LibraryAdapter adapter;
    private LocationManager locationManager;
    private double userLat = 0, userLng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_libraries);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        initViews();
        setupRecyclerView();
        requestLocation();
    }

    private void initViews() {
        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ImageButton btnRefresh = findViewById(R.id.btnRefreshLocation);
        btnRefresh.setOnClickListener(v -> requestLocation());

        recyclerLibraries = findViewById(R.id.recyclerLibraries);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvLibrariesCount = findViewById(R.id.tvLibrariesCount);
        tvCurrentLocation = findViewById(R.id.tvCurrentLocation);

        // Setup bottom navigation
        NavigationHelper.setupBottomNavigation(this, R.id.nav_libraries);
    }

    private void setupRecyclerView() {
        adapter = new LibraryAdapter();
        adapter.setOnLibraryClickListener(library -> {
            // Open search for that library's books
            Intent intent = new Intent(this, SearchBooksActivity.class);
            startActivity(intent);
        });

        recyclerLibraries.setLayoutManager(new LinearLayoutManager(this));
        recyclerLibraries.setNestedScrollingEnabled(false);
        recyclerLibraries.setAdapter(adapter);
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST);
            tvCurrentLocation.setText("Location permission needed");
            // Load with default location (Chennai)
            loadLibraries(13.0827, 80.2707);
            return;
        }

        tvCurrentLocation.setText("Getting your location...");
        progressBar.setVisibility(View.VISIBLE);

        try {
            // Try to get last known location first
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation == null) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (lastLocation != null) {
                userLat = lastLocation.getLatitude();
                userLng = lastLocation.getLongitude();
                tvCurrentLocation.setText(String.format(Locale.getDefault(),
                        "📍 Your location: %.4f, %.4f", userLat, userLng));
                loadLibraries(userLat, userLng);
            } else {
                // Request location update
                LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        userLat = location.getLatitude();
                        userLng = location.getLongitude();
                        tvCurrentLocation.setText(String.format(Locale.getDefault(),
                                "📍 Your location: %.4f, %.4f", userLat, userLng));
                        loadLibraries(userLat, userLng);
                        locationManager.removeUpdates(this);
                    }

                    @Override
                    public void onProviderDisabled(@NonNull String provider) {
                        tvCurrentLocation.setText("Location disabled, using Chennai");
                        loadLibraries(13.0827, 80.2707);
                    }
                };

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            0, 0, locationListener);
                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            0, 0, locationListener);
                } else {
                    tvCurrentLocation.setText("Using default location (Chennai)");
                    loadLibraries(13.0827, 80.2707);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Location error: " + e.getMessage());
            tvCurrentLocation.setText("Using default location");
            loadLibraries(13.0827, 80.2707);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            } else {
                Toast.makeText(this, "Location permission denied, showing all libraries",
                        Toast.LENGTH_SHORT).show();
                loadLibraries(13.0827, 80.2707);
            }
        }
    }

    private void loadLibraries(double lat, double lng) {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        String url = ApiConfig.URL_GET_LIBRARIES;
        if (lat != 0 && lng != 0) {
            url += "?lat=" + lat + "&lng=" + lng;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray librariesArray = response.getJSONArray("libraries");
                            List<Library> libraries = new ArrayList<>();

                            for (int i = 0; i < librariesArray.length(); i++) {
                                JSONObject obj = librariesArray.getJSONObject(i);
                                Library library = new Library();
                                library.setId(obj.getInt("id"));
                                library.setName(obj.optString("name", "Library"));
                                library.setLocation(obj.optString("location", ""));
                                library.setAddress(obj.optString("address", ""));
                                library.setPhone(obj.optString("phone", ""));
                                library.setOpeningHours(obj.optString("opening_hours", "9 AM - 6 PM"));
                                library.setTotalBooks(obj.optInt("total_books", 0));
                                library.setLatitude(obj.optDouble("latitude", 0));
                                library.setLongitude(obj.optDouble("longitude", 0));

                                // Calculate distance
                                if (userLat != 0 && userLng != 0 &&
                                        library.getLatitude() != 0 && library.getLongitude() != 0) {
                                    double distance = calculateDistance(userLat, userLng,
                                            library.getLatitude(), library.getLongitude());
                                    library.setDistance(distance);
                                } else {
                                    library.setDistance(obj.optDouble("distance", 0));
                                }

                                libraries.add(library);
                            }

                            // Sort by distance
                            Collections.sort(libraries, (a, b) -> Double.compare(a.getDistance(), b.getDistance()));

                            if (libraries.isEmpty()) {
                                loadSampleLibraries();
                            } else {
                                adapter.setLibraries(libraries);
                                tvLibrariesCount.setText(libraries.size() + " libraries found near you");
                                recyclerLibraries.setVisibility(View.VISIBLE);
                            }
                        } else {
                            loadSampleLibraries();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                        loadSampleLibraries();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error: " + (error.getMessage() != null ? error.getMessage() : "Network error"));
                    // Show sample libraries as fallback
                    loadSampleLibraries();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadSampleLibraries() {
        // Sample libraries for demo
        List<Library> sampleLibraries = new ArrayList<>();

        Library lib1 = new Library();
        lib1.setId(1);
        lib1.setName("SIMATS Central Library");
        lib1.setLocation("Saveetha University, Chennai");
        lib1.setOpeningHours("8 AM - 8 PM");
        lib1.setTotalBooks(5000);
        lib1.setDistance(0.5);
        lib1.setLatitude(13.0513);
        lib1.setLongitude(80.0217);
        sampleLibraries.add(lib1);

        Library lib2 = new Library();
        lib2.setId(2);
        lib2.setName("Anna Centenary Library");
        lib2.setLocation("Kotturpuram, Chennai");
        lib2.setOpeningHours("8 AM - 9 PM");
        lib2.setTotalBooks(12000);
        lib2.setDistance(5.2);
        lib2.setLatitude(13.0107);
        lib2.setLongitude(80.2417);
        sampleLibraries.add(lib2);

        Library lib3 = new Library();
        lib3.setId(3);
        lib3.setName("Connemara Public Library");
        lib3.setLocation("Egmore, Chennai");
        lib3.setOpeningHours("9 AM - 7 PM");
        lib3.setTotalBooks(8000);
        lib3.setDistance(8.5);
        lib3.setLatitude(13.0694);
        lib3.setLongitude(80.2611);
        sampleLibraries.add(lib3);

        Library lib4 = new Library();
        lib4.setId(4);
        lib4.setName("Madras Literary Society");
        lib4.setLocation("College Road, Chennai");
        lib4.setOpeningHours("10 AM - 6 PM");
        lib4.setTotalBooks(3500);
        lib4.setDistance(10.0);
        lib4.setLatitude(13.0674);
        lib4.setLongitude(80.2541);
        sampleLibraries.add(lib4);

        Library lib5 = new Library();
        lib5.setId(5);
        lib5.setName("IIT Madras Library");
        lib5.setLocation("IIT Campus, Chennai");
        lib5.setOpeningHours("8 AM - 10 PM");
        lib5.setTotalBooks(15000);
        lib5.setDistance(12.0);
        lib5.setLatitude(12.9916);
        lib5.setLongitude(80.2336);
        sampleLibraries.add(lib5);

        adapter.setLibraries(sampleLibraries);
        tvLibrariesCount.setText(sampleLibraries.size() + " libraries near you");
        tvCurrentLocation.setText("📍 Showing libraries in Chennai");
        recyclerLibraries.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerLibraries.setVisibility(View.GONE);
        tvLibrariesCount.setText("No libraries found");
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Earth's radius in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
