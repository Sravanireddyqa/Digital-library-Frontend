package com.simats.digitallibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Book Adapter for RecyclerView
 * Displays book covers from Open Library API
 */
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> books = new ArrayList<>();
    private OnBookActionListener listener;

    // Open Library Covers API
    private static final String OPEN_LIBRARY_COVER_URL = "https://covers.openlibrary.org/b/isbn/%s-M.jpg";
    // Fallback book cover placeholder
    private static final String PLACEHOLDER_COVER_URL = "https://via.placeholder.com/100x140/3B82F6/FFFFFF?text=Book";

    public interface OnBookActionListener {
        void onEditClick(Book book);

        void onDeleteClick(Book book);
    }

    public void setOnBookActionListener(OnBookActionListener listener) {
        this.listener = listener;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    public void removeBook(Book book) {
        int position = books.indexOf(book);
        if (position != -1) {
            books.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
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
        private final TextView tvBookTitle;
        private final TextView tvAuthor;
        private final TextView tvCategory;
        private final TextView tvISBN;
        private final TextView tvRating;
        private final TextView tvCopies;
        private final TextView btnEdit;
        private final TextView btnDelete;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvISBN = itemView.findViewById(R.id.tvISBN);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvCopies = itemView.findViewById(R.id.tvCopies);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Book book) {
            // Set book title
            String title = book.getTitle();
            tvBookTitle.setText(title != null && !title.isEmpty() ? title : "Untitled Book");

            // Set author
            String author = book.getAuthor();
            tvAuthor.setText("by " + (author != null && !author.isEmpty() ? author : "Unknown Author"));

            // Set category
            String category = book.getCategory();
            tvCategory.setText(category != null && !category.isEmpty() ? category : "General");

            // Set ISBN
            String isbn = book.getIsbn();
            tvISBN.setText("ISBN: " + (isbn != null && !isbn.isEmpty() ? isbn : "N/A"));

            // Set rating
            tvRating.setText(String.format("%.1f", book.getRating()));

            // Set copies
            tvCopies.setText(book.getStock() + " copies");

            // Load book cover image
            loadBookCover(book);

            // Edit button click
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(book);
                }
            });

            // Delete button click
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(book);
                }
            });
        }

        private void loadBookCover(Book book) {
            String coverUrl;

            // Priority 1: Use cover_url from database if available and valid
            String dbCoverUrl = book.getCoverUrl();
            if (dbCoverUrl != null && !dbCoverUrl.isEmpty() && !dbCoverUrl.equals("0")
                    && dbCoverUrl.startsWith("http")) {
                coverUrl = dbCoverUrl;
                android.util.Log.d("BookAdapter", "Loading cover from DB URL: " + coverUrl);
            }
            // Priority 2: Use ISBN to get cover from Open Library
            else if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                // Clean ISBN - remove dashes and spaces
                String cleanIsbn = book.getIsbn().replaceAll("[^0-9X]", "");
                coverUrl = String.format(OPEN_LIBRARY_COVER_URL, cleanIsbn);
                android.util.Log.d("BookAdapter", "Loading cover from ISBN: " + coverUrl);
            }
            // Priority 3: Use placeholder
            else {
                coverUrl = PLACEHOLDER_COVER_URL;
                android.util.Log.d("BookAdapter", "Using placeholder for: " + book.getTitle());
            }

            Glide.with(itemView.getContext())
                    .load(coverUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.ic_library_book)
                    .error(R.drawable.ic_library_book)
                    .centerCrop()
                    .into(ivBookCover);
        }
    }
}
