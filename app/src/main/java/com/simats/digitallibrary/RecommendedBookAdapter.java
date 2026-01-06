package com.simats.digitallibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for recommended books in Reader Dashboard
 */
public class RecommendedBookAdapter extends RecyclerView.Adapter<RecommendedBookAdapter.BookViewHolder> {

    private List<Book> books = new ArrayList<>();
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(Book book);

        void onReserveClick(Book book);
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.listener = listener;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommended_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivBookCover;
        private final TextView tvBookTitle, tvAuthor, tvRating, tvPrice, btnReserve;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnReserve = itemView.findViewById(R.id.btnReserve);
        }

        public void bind(Book book) {
            tvBookTitle.setText(book.getTitle());
            tvAuthor.setText(book.getAuthor());
            tvRating.setText(String.valueOf(book.getRating()));

            // Show price
            if (book.getPrice() > 0) {
                tvPrice.setText(String.format("₹%.0f", book.getPrice()));
            } else {
                tvPrice.setText("Free");
            }

            // Load cover image with fallback to Open Library API
            String coverUrl = book.getCoverUrl();
            String imageUrl;

            // Priority 1: Use cover_url from database if valid
            if (coverUrl != null && !coverUrl.isEmpty() && coverUrl.startsWith("http")) {
                imageUrl = coverUrl;
            }
            // Priority 2: Use ISBN to get cover from Open Library
            else if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                String cleanIsbn = book.getIsbn().replaceAll("[^0-9X]", "");
                imageUrl = "https://covers.openlibrary.org/b/isbn/" + cleanIsbn + "-M.jpg";
            }
            // Priority 3: Use placeholder
            else {
                imageUrl = null; // Will use placeholder
            }

            if (imageUrl != null) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_library_book)
                        .error(R.drawable.ic_library_book)
                        .centerCrop()
                        .into(ivBookCover);
            } else {
                ivBookCover.setImageResource(R.drawable.ic_library_book);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onBookClick(book);
            });

            btnReserve.setOnClickListener(v -> {
                if (listener != null)
                    listener.onReserveClick(book);
            });
        }
    }
}
