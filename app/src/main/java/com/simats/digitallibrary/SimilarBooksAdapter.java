package com.simats.digitallibrary;

import android.content.Context;
import android.content.Intent;
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
 * Adapter for similar books in book details
 */
public class SimilarBooksAdapter extends RecyclerView.Adapter<SimilarBooksAdapter.ViewHolder> {

    private List<Book> books = new ArrayList<>();
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_similar_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover;
        TextView tvBookTitle, tvAuthor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
        }

        void bind(Book book) {
            tvBookTitle.setText(book.getTitle());
            tvAuthor.setText(book.getAuthor());

            // Load cover image
            String coverUrl = book.getCoverUrl();
            if (coverUrl != null && !coverUrl.isEmpty() && coverUrl.startsWith("http")) {
                Glide.with(itemView.getContext())
                        .load(coverUrl)
                        .placeholder(R.drawable.ic_library_book)
                        .error(R.drawable.ic_library_book)
                        .centerCrop()
                        .into(ivBookCover);
            } else {
                ivBookCover.setImageResource(R.drawable.ic_library_book);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(book);
                }
            });
        }
    }
}
