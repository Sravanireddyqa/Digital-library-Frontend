package com.simats.digitallibrary;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Library Adapter for RecyclerView
 */
public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder> {

    private List<Library> libraries = new ArrayList<>();
    private OnLibraryClickListener listener;

    public interface OnLibraryClickListener {
        void onViewBooksClick(Library library);
    }

    public void setOnLibraryClickListener(OnLibraryClickListener listener) {
        this.listener = listener;
    }

    public void setLibraries(List<Library> libraries) {
        this.libraries = libraries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library, parent, false);
        return new LibraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
        Library library = libraries.get(position);
        holder.bind(library);
    }

    @Override
    public int getItemCount() {
        return libraries.size();
    }

    class LibraryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLibraryName;
        private final TextView tvLocation;
        private final TextView tvDistance;
        private final TextView tvBooksCount;
        private final TextView tvHours;
        private final TextView btnViewBooks;
        private final TextView btnGetDirections;

        LibraryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLibraryName = itemView.findViewById(R.id.tvLibraryName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvBooksCount = itemView.findViewById(R.id.tvBooksCount);
            tvHours = itemView.findViewById(R.id.tvHours);
            btnViewBooks = itemView.findViewById(R.id.btnViewBooks);
            btnGetDirections = itemView.findViewById(R.id.btnGetDirections);
        }

        void bind(Library library) {
            tvLibraryName.setText(library.getName());
            tvLocation.setText(library.getLocation());

            // Distance
            if (library.getDistance() > 0) {
                tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", library.getDistance()));
            } else {
                tvDistance.setText("Nearby");
            }

            // Books count
            int books = library.getTotalBooks();
            if (books > 0) {
                tvBooksCount.setText(books + " Books");
            } else {
                tvBooksCount.setText("Many Books");
            }

            // Hours
            String hours = library.getOpeningHours();
            if (hours != null && !hours.isEmpty()) {
                tvHours.setText(hours);
            } else {
                tvHours.setText("9 AM - 6 PM");
            }

            // View Books
            btnViewBooks.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewBooksClick(library);
                }
            });

            // Get Directions - Open Google Maps
            btnGetDirections.setOnClickListener(v -> {
                openDirections(itemView.getContext(), library);
            });
        }

        private void openDirections(Context context, Library library) {
            try {
                String uri;
                if (library.getLatitude() != 0 && library.getLongitude() != 0) {
                    // Open with coordinates
                    uri = String.format(Locale.US,
                            "google.navigation:q=%f,%f",
                            library.getLatitude(),
                            library.getLongitude());
                } else {
                    // Open with address/name
                    String query = library.getName() + " " + library.getLocation();
                    uri = "geo:0,0?q=" + Uri.encode(query);
                }

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setPackage("com.google.android.apps.maps");

                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                } else {
                    // Fallback to browser
                    String browserUri = "https://www.google.com/maps/search/?api=1&query=" +
                            Uri.encode(library.getName() + " " + library.getLocation());
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri)));
                }
            } catch (Exception e) {
                Toast.makeText(context, "Unable to open maps", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
