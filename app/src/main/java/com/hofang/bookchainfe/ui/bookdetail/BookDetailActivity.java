package com.hofang.bookchainfe.ui.bookdetail;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Book;
import com.hofang.bookchainfe.model.Review;
import com.hofang.bookchainfe.utils.CartManager;
import com.hofang.bookchainfe.utils.TokenManager;

import java.util.ArrayList;

public class BookDetailActivity extends AppCompatActivity implements ReviewAdapter.OnReviewActionListener {

    private ImageView ivBookCover;
    private TextView tvBookTitle;
    private TextView tvCategoryTitle;
    private TextView tvAuthor;
    private TextView tvCategory;
    private TextView tvRating;
    private TextView tvPrice;
    private TextView tvDescription;
    private Button btnAddToCart;
    private ImageButton btnBack;
    private ImageButton btnCart;
    
    // Reviews
    private FrameLayout containerReviewForm;
    private RecyclerView rvReviews;
    private TextView tvReviewsCount;
    private TextView tvNoReviews;
    private ReviewAdapter reviewAdapter;
    private ArrayList<Review> reviewList;
    
    // Review form views
    private View reviewFormView;
    private RatingBar ratingBarInput;
    private EditText etReviewComment;
    private Button btnSubmitReview;

    private Book book;
    private String currentUserId;
    private boolean hasCompletedOrder = false; // TODO: Check from backend

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // Get current user ID
        TokenManager tokenManager = new TokenManager(this);
        currentUserId = tokenManager.getUserId();
        
        // TODO: Check if user has completed order for this book
        hasCompletedOrder = true; // Mock data - set to true to test

        initViews();
        loadBookData();
        setupReviews();
        setupListeners();
    }

    private void initViews() {
        ivBookCover = findViewById(R.id.iv_book_cover);
        tvBookTitle = findViewById(R.id.tv_book_title);
        tvCategoryTitle = findViewById(R.id.tv_category_title);
        tvAuthor = findViewById(R.id.tv_author);
        tvCategory = findViewById(R.id.tv_category);
        tvRating = findViewById(R.id.tv_rating);
        tvPrice = findViewById(R.id.tv_price);
        tvDescription = findViewById(R.id.tv_description);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        btnBack = findViewById(R.id.btn_back);
        btnCart = findViewById(R.id.btn_cart);
        
        // Reviews
        containerReviewForm = findViewById(R.id.container_review_form);
        rvReviews = findViewById(R.id.rv_reviews);
        tvReviewsCount = findViewById(R.id.tv_reviews_count);
        tvNoReviews = findViewById(R.id.tv_no_reviews);
        
        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                finish();
                return true;
            } else if (itemId == R.id.nav_categories) {
                // TODO: Navigate to categories
                Toast.makeText(this, "Categories", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_cart) {
                // Navigate to cart
                Intent cartIntent = new Intent(BookDetailActivity.this, com.hofang.bookchainfe.ui.cart.CartActivity.class);
                startActivity(cartIntent);
                return true;
            } else if (itemId == R.id.nav_account) {
                // TODO: Navigate to account
                Toast.makeText(this, "Account", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void loadBookData() {
        // Get book data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            String bookId = intent.getStringExtra("bookId");
            String title = intent.getStringExtra("title");
            String author = intent.getStringExtra("author");
            String categoryName = intent.getStringExtra("category");
            Double price = intent.getDoubleExtra("price", 0.0);
            Integer discount = intent.getIntExtra("discount", 0);
            String description = intent.getStringExtra("description");
            String coverImage = intent.getStringExtra("coverImage");
            Double rating = intent.getDoubleExtra("rating", 4.0);
            int coverResId = intent.getIntExtra("coverResId", 0);

            // Create Book object
            book = new Book(bookId, title, author, categoryName, price, discount, description, coverImage, rating);
            
            // If no coverImage URL but has coverResId, set the resource URI
            if ((coverImage == null || coverImage.isEmpty()) && coverResId != 0) {
                // Convert drawable resource to URI string for Glide
                book.setCoverImage("android.resource://" + getPackageName() + "/" + coverResId);
            }
            
            // Set stock (default value for now, should come from backend)
            book.setStock(50);

            // Display data
            tvBookTitle.setText(title);
            tvCategoryTitle.setText(categoryName);
            tvAuthor.setText(author);
            tvCategory.setText(categoryName);
            tvRating.setText(String.format("%.2f/5", rating));
            
            // Display price with discount if applicable
            if (discount != null && discount > 0) {
                double finalPrice = price * (100 - discount) / 100.0;
                tvPrice.setText(String.format("$%.2f (-%d%%)", finalPrice, discount));
            } else {
                tvPrice.setText(String.format("$%.2f", price));
            }
            
            tvDescription.setText(description != null ? description : "No description available.");

            // Set book cover image
            if (coverResId != 0) {
                ivBookCover.setImageResource(coverResId);
            } else if (coverImage != null && !coverImage.isEmpty()) {
                // Load from URL using Glide
                com.bumptech.glide.Glide.with(this)
                    .load(coverImage)
                    .placeholder(R.drawable.classic_book)
                    .error(R.drawable.classic_book)
                    .into(ivBookCover);
            } else {
                ivBookCover.setImageResource(R.drawable.classic_book);
            }
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCart.setOnClickListener(v -> {
            // TODO: Navigate to Cart
            Toast.makeText(this, "Navigate to Cart", Toast.LENGTH_SHORT).show();
        });

        btnAddToCart.setOnClickListener(v -> {
            if (book != null) {
                addToCart();
            }
        });
    }

    private void addToCart() {
        // Show Add to Cart bottom sheet
        AddToCartBottomSheet bottomSheet = AddToCartBottomSheet.newInstance(book, (selectedBook, quantity) -> {
            // Save to cart using CartManager
            CartManager cartManager = new CartManager(this);
            cartManager.addToCart(selectedBook, quantity);
            
            // Debug: Check cart items count
            int cartCount = cartManager.getCartItemsCount();
            android.util.Log.d("BookDetail", "Cart items count after add: " + cartCount);
            
            // Show confirmation
            Toast.makeText(this, 
                selectedBook.getTitle() + " (" + quantity + ") added to cart! Total: " + cartCount, 
                Toast.LENGTH_SHORT).show();
        });
        bottomSheet.show(getSupportFragmentManager(), "AddToCartBottomSheet");
    }
    
    private void setupReviews() {
        // Initialize review list
        reviewList = new ArrayList<>();
        
        // TODO: Load reviews from backend
        // For now, add mock reviews
        loadMockReviews();
        
        // Calculate and update average rating from reviews
        updateAverageRating();
        
        // Setup RecyclerView
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(this, reviewList, currentUserId, this);
        rvReviews.setAdapter(reviewAdapter);
        
        // Update reviews count
        updateReviewsCount();
        
        // Show/hide review form based on order status
        if (hasCompletedOrder && !hasUserReviewed()) {
            showReviewForm();
        }
        
        // Show/hide empty message
        if (reviewList.isEmpty()) {
            tvNoReviews.setVisibility(View.VISIBLE);
            rvReviews.setVisibility(View.GONE);
        } else {
            tvNoReviews.setVisibility(View.GONE);
            rvReviews.setVisibility(View.VISIBLE);
        }
    }
    
    private void loadMockReviews() {
        // Add some mock reviews
        Review review1 = new Review("1", "user1", "book1", 5, "Excellent book! Highly recommended.", "2024-11-08T10:30:00");
        review1.setUser(createMockUser("Alice Johnson", "alice"));
        
        Review review2 = new Review("2", "user2", "book1", 4, "Great read, very enjoyable.", "2024-11-09T14:20:00");
        review2.setUser(createMockUser("Bob Smith", "bob"));
        
        Review review3 = new Review("3", currentUserId, "book1", 5, "One of my favorite classics!", "2024-11-10T09:15:00");
        review3.setUser(createMockUser("You", "hehehe"));
        
        reviewList.add(review1);
        reviewList.add(review2);
        // Uncomment to test user's own review
        // reviewList.add(review3);
    }
    
    private com.hofang.bookchainfe.model.User createMockUser(String fullName, String username) {
        com.hofang.bookchainfe.model.User user = new com.hofang.bookchainfe.model.User();
        user.setFullName(fullName);
        user.setUsername(username);
        return user;
    }
    
    private boolean hasUserReviewed() {
        if (currentUserId == null) return false;
        
        for (Review review : reviewList) {
            if (currentUserId.equals(review.getUserId())) {
                return true;
            }
        }
        return false;
    }
    
    private void updateAverageRating() {
        if (reviewList == null || reviewList.isEmpty()) {
            // No reviews, use default or keep original rating
            if (book != null && book.getRating() != null) {
                tvRating.setText(String.format("%.2f/5", book.getRating()));
            } else {
                tvRating.setText("0.00/5");
            }
            return;
        }
        
        // Calculate average from all reviews
        double totalRating = 0.0;
        for (Review review : reviewList) {
            totalRating += review.getRating();
        }
        double averageRating = totalRating / reviewList.size();
        
        // Update UI and book object
        tvRating.setText(String.format("%.2f/5", averageRating));
        if (book != null) {
            book.setRating(averageRating);
        }
    }
    
    private void showReviewForm() {
        reviewFormView = LayoutInflater.from(this).inflate(R.layout.item_review_form, containerReviewForm, false);
        containerReviewForm.addView(reviewFormView);
        containerReviewForm.setVisibility(View.VISIBLE);
        
        ratingBarInput = reviewFormView.findViewById(R.id.rating_bar_input);
        etReviewComment = reviewFormView.findViewById(R.id.et_review_comment);
        btnSubmitReview = reviewFormView.findViewById(R.id.btn_submit_review);
        
        btnSubmitReview.setOnClickListener(v -> submitReview());
    }
    
    private void hideReviewForm() {
        containerReviewForm.removeAllViews();
        containerReviewForm.setVisibility(View.GONE);
    }
    
    private void submitReview() {
        float rating = ratingBarInput.getRating();
        String comment = etReviewComment.getText().toString().trim();
        
        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (comment.isEmpty()) {
            Toast.makeText(this, "Please write a review", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // TODO: Submit review to backend
        // For now, add it locally
        Review newReview = new Review(
            String.valueOf(System.currentTimeMillis()),
            currentUserId,
            book.getId(),
            (int) rating,
            comment,
            new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new java.util.Date())
        );
        
        TokenManager tokenManager = new TokenManager(this);
        newReview.setUser(createMockUser(tokenManager.getFullName(), tokenManager.getUsername()));
        
        reviewList.add(0, newReview); // Add to top
        reviewAdapter.updateReviews(reviewList);
        
        // Update average rating
        updateAverageRating();
        
        // Hide form and show success message
        hideReviewForm();
        updateReviewsCount();
        tvNoReviews.setVisibility(View.GONE);
        rvReviews.setVisibility(View.VISIBLE);
        
        Toast.makeText(this, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
    }
    
    private void updateReviewsCount() {
        tvReviewsCount.setText("(" + reviewList.size() + ")");
    }
    
    @Override
    public void onEditReview(Review review) {
        // Show dialog to edit review
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.item_review_form, null);
        
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar_input);
        EditText etComment = dialogView.findViewById(R.id.et_review_comment);
        
        ratingBar.setRating(review.getRating());
        etComment.setText(review.getComment());
        
        builder.setView(dialogView)
            .setTitle("Edit Review")
            .setPositiveButton("Update", (dialog, which) -> {
                int newRating = (int) ratingBar.getRating();
                String newComment = etComment.getText().toString().trim();
                
                if (newRating > 0 && !newComment.isEmpty()) {
                    // TODO: Update review on backend
                    review.setRating(newRating);
                    review.setComment(newComment);
                    reviewAdapter.notifyDataSetChanged();
                    
                    // Update average rating after edit
                    updateAverageRating();
                    
                    Toast.makeText(this, "Review updated!", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    public void onDeleteReview(Review review) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Review")
            .setMessage("Are you sure you want to delete this review?")
            .setPositiveButton("Delete", (dialog, which) -> {
                // TODO: Delete review from backend
                reviewList.remove(review);
                reviewAdapter.updateReviews(reviewList);
                updateReviewsCount();
                
                // Update average rating after delete
                updateAverageRating();
                
                if (reviewList.isEmpty()) {
                    tvNoReviews.setVisibility(View.VISIBLE);
                    rvReviews.setVisibility(View.GONE);
                }
                
                // Show form again if user has completed order
                if (hasCompletedOrder) {
                    showReviewForm();
                }
                
                Toast.makeText(this, "Review deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
