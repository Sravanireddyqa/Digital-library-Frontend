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
 * Adapter for wishlist books with remove functionality
 */
public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private List<Book> books = new ArrayList<>();
    private OnWishlistActionListener listener;

    public interface OnWishlistActionListener {
        void onBookClick(Book book);

        void onReserveClick(Book book);

        void onRemoveClick(Book book, int position);
    }

    public void setOnWishlistActionListener(OnWishlistActionListener listener) {
        this.listener = listener;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    public void removeBook(int position) {
        if (position >= 0 && position < books.size()) {
            books.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wishlist_book, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class WishlistViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivBookCover;
        private final TextView tvBookTitle, tvAuthor, tvRating, tvPrice, btnReserve, btnRemove;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnReserve = itemView.findViewById(R.id.btnReserve);
            btnRemove = itemView.findViewById(R.id.btnRemove);
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

            // Load cover image
            String coverUrl = book.getCoverUrl();
            if (coverUrl != null && !coverUrl.isEmpty() && coverUrl.startsWith("http")) {
                Glide.with(itemView.getContext())
                        .load(coverUrl)
                        .placeholder(R.drawable.ic_library_book)
                        .error(R.drawable.ic_library_book)
                        .into(ivBookCover);
            } else {
                ivBookCover.setImageResource(R.drawable.ic_library_book);
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(book);
                }
            });

            btnReserve.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReserveClick(book);
                }
            });

            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveClick(book, getAdapterPosition());
                }
            });
        }
    }
}
