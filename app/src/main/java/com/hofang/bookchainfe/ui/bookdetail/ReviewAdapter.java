package com.hofang.bookchainfe.ui.bookdetail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Review;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private ArrayList<Review> reviewList;
    private String currentUserId;
    private OnReviewActionListener listener;

    public interface OnReviewActionListener {
        void onEditReview(Review review);
        void onDeleteReview(Review review);
    }

    public ReviewAdapter(Context context, ArrayList<Review> reviewList, String currentUserId, OnReviewActionListener listener) {
        this.context = context;
        this.reviewList = reviewList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        // Set user name
        holder.tvUsername.setText(review.getUserName());

        // Set rating
        holder.ratingBar.setRating(review.getRating() != null ? review.getRating() : 0);

        // Set comment
        holder.tvReviewComment.setText(review.getComment());

        // Set date
        holder.tvReviewDate.setText(getTimeAgo(review.getCreatedAt()));

        // Show menu button only for current user's review
        if (currentUserId != null && currentUserId.equals(review.getUserId())) {
            holder.btnReviewMenu.setVisibility(View.VISIBLE);
            holder.btnReviewMenu.setOnClickListener(v -> showPopupMenu(v, review));
        } else {
            holder.btnReviewMenu.setVisibility(View.GONE);
        }
    }

    private void showPopupMenu(View view, Review review) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.menu_review_options);
        
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit_review) {
                if (listener != null) {
                    listener.onEditReview(review);
                }
                return true;
            } else if (id == R.id.action_delete_review) {
                if (listener != null) {
                    listener.onDeleteReview(review);
                }
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private String getTimeAgo(String createdAt) {
        if (createdAt == null) return "Recently";
        
        try {
            // Try multiple date formats to handle different ISO 8601 variations
            Date date = null;
            String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",  // With milliseconds and Z
                "yyyy-MM-dd'T'HH:mm:ss'Z'",      // Without milliseconds, with Z
                "yyyy-MM-dd'T'HH:mm:ss.SSS",     // With milliseconds, no Z
                "yyyy-MM-dd'T'HH:mm:ss",         // Basic format
                "yyyy-MM-dd HH:mm:ss"            // Alternative format
            };
            
            for (String format : formats) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    date = sdf.parse(createdAt);
                    if (date != null) break;
                } catch (Exception e) {
                    // Try next format
                }
            }
            
            if (date != null) {
                long timeInMillis = date.getTime();
                long currentTimeMillis = System.currentTimeMillis();
                long diff = currentTimeMillis - timeInMillis;
                
                long seconds = diff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;
                long months = days / 30;
                long years = days / 365;
                
                if (years > 0) {
                    return years + " year" + (years > 1 ? "s" : "") + " ago";
                } else if (months > 0) {
                    return months + " month" + (months > 1 ? "s" : "") + " ago";
                } else if (days > 0) {
                    return days + " day" + (days > 1 ? "s" : "") + " ago";
                } else if (hours > 0) {
                    return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
                } else if (minutes > 0) {
                    return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
                } else {
                    return "Just now";
                }
            }
        } catch (Exception e) {
            android.util.Log.e("ReviewAdapter", "Error parsing date: " + createdAt + " - " + e.getMessage());
        }
        
        return "Recently";
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void updateReviews(ArrayList<Review> newReviews) {
        this.reviewList = newReviews;
        notifyDataSetChanged();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar;
        TextView tvUsername;
        TextView tvReviewDate;
        RatingBar ratingBar;
        TextView tvReviewComment;
        ImageButton btnReviewMenu;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvReviewDate = itemView.findViewById(R.id.tv_review_date);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            tvReviewComment = itemView.findViewById(R.id.tv_review_comment);
            btnReviewMenu = itemView.findViewById(R.id.btn_review_menu);
        }
    }
}
