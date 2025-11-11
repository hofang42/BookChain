package com.hofang.bookchainfe.ui.bookdetail;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.model.Book;
import com.hofang.bookchainfe.model.Branch; // <-- THÊM IMPORT
import com.hofang.bookchainfe.model.CartAddRequest;
import com.hofang.bookchainfe.model.ChatContext;
import com.hofang.bookchainfe.model.Review;
import com.hofang.bookchainfe.model.ReviewRequest;
import com.hofang.bookchainfe.model.UploadResponse;
import com.hofang.bookchainfe.model.User;
import com.google.gson.JsonObject;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.CartApiService;
import com.hofang.bookchainfe.network.ReviewApiService;
import com.hofang.bookchainfe.ui.chatbot.FloatingChatButtonHelper;
import com.hofang.bookchainfe.utils.TokenManager;

import java.text.NumberFormat; // <-- THÊM IMPORT
import java.util.ArrayList;
import java.util.Locale; // <-- THÊM IMPORT

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailFragment extends Fragment implements ReviewAdapter.OnReviewActionListener {

    private ImageView ivBookCover;
    private TextView tvBookTitle;
    private TextView tvCategoryTitle;
    private TextView tvAuthor;
    private TextView tvCategory;
    private TextView tvRating;
    private TextView tvPrice;
    private TextView tvDescription;
    private Button btnAddToCart;
    private Button btnViewBranches;
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
    private boolean hasCompletedOrder = false;
    private CartApiService cartApiService;
    private ReviewApiService reviewApiService;

    // --- THÊM CÁC BIẾN NÀY ---
    private String selectedBranchId = null;
    private String selectedBranchName = null;
    private int selectedStock = 0;
    private NumberFormat currencyFormatter; // Để format tiền VN
    // --- KẾT THÚC THÊM MỚI ---

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_book_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TokenManager tokenManager = new TokenManager(requireContext());
        currentUserId = tokenManager.getUserId();

        // Khởi tạo định dạng tiền
        Locale localeVN = new Locale("vi", "VN");
        currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);

        hasCompletedOrder = true; // Mock data

        initViews(view);
        cartApiService = ApiConfig.getCartApiService();
        reviewApiService = ApiConfig.getReviewApiService();
        loadBookData();
        setupReviews();
        setupListeners();
        
        // Add floating chat button - will be configured with book context after loadBookData
        if (getArguments() != null) {
            String bookId = getArguments().getString("bookId");
            String title = getArguments().getString("title");
            ChatContext chatContext = new ChatContext("Chi tiết sách", bookId, title);
            ViewGroup parentView = (ViewGroup) requireActivity().findViewById(android.R.id.content);
            FloatingChatButtonHelper.addFloatingChatButton(requireActivity(), parentView, chatContext);
        }
    }

    private void initViews(View view) {
        ivBookCover = view.findViewById(R.id.iv_book_cover);
        tvBookTitle = view.findViewById(R.id.tv_book_title);
        tvCategoryTitle = view.findViewById(R.id.tv_category_title);
        tvAuthor = view.findViewById(R.id.tv_author);
        tvCategory = view.findViewById(R.id.tv_category);
        tvRating = view.findViewById(R.id.tv_rating);
        tvPrice = view.findViewById(R.id.tv_price);
        tvDescription = view.findViewById(R.id.tv_description);
        btnAddToCart = view.findViewById(R.id.btn_add_to_cart);
        btnViewBranches = view.findViewById(R.id.btn_view_branches);
        btnBack = view.findViewById(R.id.btn_back);
        btnCart = view.findViewById(R.id.btn_cart);

        containerReviewForm = view.findViewById(R.id.container_review_form);
        rvReviews = view.findViewById(R.id.rv_reviews);
        tvReviewsCount = view.findViewById(R.id.tv_reviews_count);
        tvNoReviews = view.findViewById(R.id.tv_no_reviews);
    }

    private void loadBookData() {
        Bundle args = getArguments();
        if (args != null) {
            String bookId = args.getString("bookId");
            String title = args.getString("title");
            String author = args.getString("author");
            String categoryName = args.getString("category");
            double price = args.getFloat("price", 0.0f);
            int discount = args.getInt("discount", 0);
            String description = args.getString("description");
            double rating = args.getFloat("rating", 4.0f);

            // --- BẮT ĐẦU SỬA LỖI ---
            // Chỉ lấy một key duy nhất là "coverImage"
            String coverImage = args.getString("coverImage");
            int coverResId = args.getInt("coverResId", 0);

            book = new Book(bookId, title, author, categoryName, price, discount, description, coverImage, rating);

            // --- SỬA LỖI LOGIC HÌNH ẢNH ---
            if ((coverImage == null || coverImage.isEmpty()) && coverResId != 0) {
                // (Chỉ dùng cho data hard-code)
                book.setCoverImage("android.resource://" + requireContext().getPackageName() + "/" + coverResId);
            }
            // (Không cần 'else if' cho coverUrl nữa)

            tvBookTitle.setText(title);
            tvCategoryTitle.setText(categoryName);
            // ... (code set text khác giữ nguyên)
            tvPrice.setText(currencyFormatter.format(price)); // (Đã sửa)

            tvDescription.setText(description != null && !description.isEmpty() ? description : "No description available.");

            // --- SỬA LỖI LOGIC HIỂN THỊ ẢNH (GLIDE) ---
            if (coverResId != 0) {
                // Hiển thị ảnh hard-code
                ivBookCover.setImageResource(coverResId);
            } else if (coverImage != null && !coverImage.isEmpty()) {
                // Hiển thị ảnh từ URL (từ "Best Selling" VÀ "Search")
                Glide.with(this)
                        .load(coverImage) // Luôn dùng 'coverImage'
                        .placeholder(R.drawable.classic_book)
                        .error(R.drawable.classic_book)
                        .into(ivBookCover);
            } else {
                // Nếu cả hai đều null/rỗng, hiển thị placeholder
                ivBookCover.setImageResource(R.drawable.classic_book);
            }
            // --- KẾT THÚC SỬA LỖI ---
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnCart.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_cart);
        });

        btnAddToCart.setOnClickListener(v -> {
            if (book != null) {
                // --- THAY ĐỔI: Kiểm tra chi nhánh trước ---
                if (selectedBranchId == null) {
                    Toast.makeText(requireContext(), "Vui lòng chọn chi nhánh trước", Toast.LENGTH_SHORT).show();
                    // Tự động mở chọn chi nhánh
                    showBranchesMap();
                } else {
                    // Nếu đã chọn, mở bottom sheet thêm giỏ hàng
                    showAddToCartSheet();
                }
                // --- KẾT THÚC THAY ĐỔI ---
            }
        });

        btnViewBranches.setOnClickListener(v -> {
            if (book != null && book.getId() != null) {
                showBranchesMap();
            } else {
                Toast.makeText(requireContext(), "Book information not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- ĐỔI TÊN HÀM NÀY: addToCart() -> showAddToCartSheet() ---
    private void showAddToCartSheet() {
        // Truyền stock và tên chi nhánh đã chọn vào
        AddToCartBottomSheet bottomSheet = AddToCartBottomSheet.newInstance(
                book,
                selectedStock, // <-- Truyền tồn kho của chi nhánh
                selectedBranchName, // <-- Truyền tên chi nhánh
                (selectedBook, quantity) -> {
                    // Đây là listener callback khi bấm nút "Add" trong bottom sheet

                    if (selectedBook == null || selectedBook.getId() == null || selectedBook.getId().isEmpty()) {
                        Toast.makeText(requireContext(), "Lỗi: Không có thông tin sách", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // --- THAY ĐỔI: Thêm branchId vào request ---
                    CartAddRequest request = new CartAddRequest(selectedBook.getId(), quantity, selectedBranchId);

                    cartApiService.addToCart(request).enqueue(new Callback<UploadResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<UploadResponse> call, @NonNull Response<UploadResponse> response) {
                            if (getContext() == null) return;

                            if (response.isSuccessful() && response.body() != null) {
                                String message = response.body().getMessage() != null ?
                                        response.body().getMessage() : "Đã thêm vào giỏ hàng";
                                Toast.makeText(getContext(),
                                        selectedBook.getTitle() + " (" + quantity + ") - " + message,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Xử lý lỗi (ví dụ: đổi chi nhánh)
                                String errorMessage = "Không thể thêm vào giỏ hàng";
                                if (response.code() == 401) {
                                    errorMessage = "Vui lòng đăng nhập để thêm vào giỏ hàng";
                                } else if (response.errorBody() != null) {
                                    try {
                                        // Cố gắng đọc thông báo lỗi từ server
                                        String errorString = response.errorBody().string();
                                        JsonObject jsonObject = com.google.gson.JsonParser.parseString(errorString).getAsJsonObject();
                                        if (jsonObject.has("message")) {
                                            errorMessage = jsonObject.get("message").getAsString();
                                        }
                                    } catch (Exception e) {
                                        Log.e("BookDetailFragment", "Error parsing error body", e);
                                    }
                                }
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<UploadResponse> call, @NonNull Throwable t) {
                            if (getContext() == null) return;
                            android.util.Log.e("BookDetailFragment", "addToCart failure: " + t.getMessage());
                            Toast.makeText(getContext(), "Lỗi mạng, không thể thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
        bottomSheet.show(getParentFragmentManager(), "AddToCartBottomSheet");
    }

    private void showBranchesMap() {
        BranchesMapBottomSheet bottomSheet = BranchesMapBottomSheet.newInstance(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCoverImage()
        );

        // --- THÊM MỚI: Listener để nhận chi nhánh đã chọn ---
        bottomSheet.setOnBranchSelectedListener((branch, stock) -> {
            this.selectedBranchId = branch.getId();
            this.selectedBranchName = branch.getName();
            this.selectedStock = stock;

            // Cập nhật UI (ví dụ: đổi text của nút)
            btnViewBranches.setText("Đã chọn: " + branch.getName());
            Toast.makeText(getContext(), "Đã chọn chi nhánh: " + branch.getName(), Toast.LENGTH_SHORT).show();

            // Tự động mở giỏ hàng sau khi chọn chi nhánh
            showAddToCartSheet();
        });
        // --- KẾT THÚC THÊM MỚI ---

        bottomSheet.show(getParentFragmentManager(), "BranchesMapBottomSheet");
    }
    
    private void setupReviews() {
        // Initialize review list
        reviewList = new ArrayList<>();
        
        // Setup RecyclerView first
        rvReviews.setLayoutManager(new LinearLayoutManager(requireContext()));
        reviewAdapter = new ReviewAdapter(requireContext(), reviewList, currentUserId, this);
        rvReviews.setAdapter(reviewAdapter);
        
        // Fetch reviews from API
        fetchReviewsFromApi();
    }
    
    private void fetchReviewsFromApi() {
        if (book == null || book.getId() == null || book.getId().isEmpty()) {
            android.util.Log.e("BookDetailFragment", "Cannot fetch reviews: book ID is null");
            updateReviewsUI();
            return;
        }
        
        reviewApiService.getBookReviews(book.getId()).enqueue(new Callback<ApiResponse<java.util.List<Review>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<java.util.List<Review>>> call, 
                                 @NonNull Response<ApiResponse<java.util.List<Review>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<java.util.List<Review>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        reviewList.clear();
                        reviewList.addAll(apiResponse.getData());
                        
                        // Force parse userId for each review to ensure user info is available
                        // Backend populates userId as User object, but Gson may deserialize it as LinkedTreeMap
                        // The Review.getUser() method will handle parsing automatically
                        for (Review review : reviewList) {
                            // Trigger parsing by calling getUser() - this will parse Map/JsonObject to User if needed
                            com.hofang.bookchainfe.model.User user = review.getUser();
                            android.util.Log.d("BookDetailFragment", "Review user: " + (user != null ? review.getUserName() : "null"));
                        }
                        
                        reviewAdapter.notifyDataSetChanged();
                        android.util.Log.d("BookDetailFragment", "Loaded " + reviewList.size() + " reviews from API");
                    } else {
                        android.util.Log.w("BookDetailFragment", "API returned unsuccessful: " + apiResponse.getMessage());
                    }
                } else {
                    android.util.Log.e("BookDetailFragment", "Failed to fetch reviews: " + response.code());
                }
                updateReviewsUI();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<java.util.List<Review>>> call, @NonNull Throwable t) {
                android.util.Log.e("BookDetailFragment", "Error fetching reviews: " + t.getMessage());
                updateReviewsUI();
            }
        });
    }
    
    private void updateReviewsUI() {
        // Calculate and update average rating from reviews
        updateAverageRating();
        
        // Update reviews count
        updateReviewsCount();
        
        // Show/hide review form based on order status
        if (hasCompletedOrder && !hasUserReviewed()) {
            showReviewForm();
        } else {
            hideReviewForm();
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
        reviewFormView = LayoutInflater.from(requireContext()).inflate(R.layout.item_review_form, containerReviewForm, false);
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
            Toast.makeText(requireContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (comment.isEmpty()) {
            Toast.makeText(requireContext(), "Please write a review", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create review request
        ReviewRequest request = new ReviewRequest(book.getId(), (int) rating, comment);
        
        // Call API to create review
        reviewApiService.createReview(request).enqueue(new Callback<ApiResponse<Review>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Review>> call, 
                                 @NonNull Response<ApiResponse<Review>> response) {
                if (getContext() == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Review> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Review newReview = apiResponse.getData();
                        reviewList.add(0, newReview); // Add to top
                        reviewAdapter.updateReviews(reviewList);
                        
                        // Update average rating
                        updateAverageRating();
                        
                        // Hide form and show success message
                        hideReviewForm();
                        updateReviewsCount();
                        tvNoReviews.setVisibility(View.GONE);
                        rvReviews.setVisibility(View.VISIBLE);
                        
                        Toast.makeText(getContext(), "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                        
                        // Refresh reviews to get updated data
                        fetchReviewsFromApi();
                    } else {
                        Toast.makeText(getContext(), 
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to submit review", 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle errors
                    if (response.code() == 401) {
                        Toast.makeText(getContext(), "Please login to submit a review", Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 403) {
                        Toast.makeText(getContext(), "You can only review books from completed orders", Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 400) {
                        Toast.makeText(getContext(), "You have already reviewed this book", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to submit review", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Review>> call, @NonNull Throwable t) {
                if (getContext() == null) return;
                android.util.Log.e("BookDetailFragment", "Error submitting review: " + t.getMessage());
                Toast.makeText(getContext(), "Network error, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateReviewsCount() {
        tvReviewsCount.setText("(" + reviewList.size() + ")");
    }
    
    @Override
    public void onEditReview(Review review) {
        // Show dialog to edit review
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.item_review_form, null);
        
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar_input);
        EditText etComment = dialogView.findViewById(R.id.et_review_comment);
        Button btnSubmitReview = dialogView.findViewById(R.id.btn_submit_review);
        
        ratingBar.setRating(review.getRating());
        etComment.setText(review.getComment());
        btnSubmitReview.setText("Update Review");
        
        AlertDialog dialog = builder.setView(dialogView)
            .setTitle("Edit Review")
            .setCancelable(true)
            .create();
        
        // Handle click outside to dismiss
        dialog.setCanceledOnTouchOutside(true);
        
        // Attach API to submit button
        btnSubmitReview.setOnClickListener(v -> {
            int newRating = (int) ratingBar.getRating();
            String newComment = etComment.getText().toString().trim();
            
            if (newRating > 0 && !newComment.isEmpty()) {
                // Create update request
                ReviewRequest request = new ReviewRequest(newRating, newComment);
                
                // Call API to update review
                reviewApiService.updateReview(review.getId(), request).enqueue(new Callback<ApiResponse<Review>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Review>> call, 
                                         @NonNull Response<ApiResponse<Review>> response) {
                        if (getContext() == null) return;
                        
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Review> apiResponse = response.body();
                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                Review updatedReview = apiResponse.getData();
                                // Update the review in the list
                                int index = reviewList.indexOf(review);
                                if (index >= 0) {
                                    reviewList.set(index, updatedReview);
                                    reviewAdapter.updateReviews(reviewList);
                                }
                                
                                // Update average rating after edit
                                updateAverageRating();
                                
                                Toast.makeText(getContext(), "Review updated!", Toast.LENGTH_SHORT).show();
                                
                                // Close dialog
                                dialog.dismiss();
                                
                                // Refresh reviews to get updated data
                                fetchReviewsFromApi();
                            } else {
                                Toast.makeText(getContext(), 
                                    apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to update review", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (response.code() == 401) {
                                Toast.makeText(getContext(), "Please login to update review", Toast.LENGTH_SHORT).show();
                            } else if (response.code() == 403) {
                                Toast.makeText(getContext(), "You can only edit your own reviews", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to update review", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Review>> call, @NonNull Throwable t) {
                        if (getContext() == null) return;
                        android.util.Log.e("BookDetailFragment", "Error updating review: " + t.getMessage());
                        Toast.makeText(getContext(), "Network error, please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Please provide rating and comment", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }
    
    @Override
    public void onDeleteReview(Review review) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Review")
            .setMessage("Are you sure you want to delete this review?")
            .setPositiveButton("Delete", (dialog, which) -> {
                // Call API to delete review
                reviewApiService.deleteReview(review.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Void>> call, 
                                         @NonNull Response<ApiResponse<Void>> response) {
                        if (getContext() == null) return;
                        
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
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
                                
                                Toast.makeText(getContext(), "Review deleted", Toast.LENGTH_SHORT).show();
                                
                                // Refresh reviews to get updated data
                                fetchReviewsFromApi();
                            } else {
                                Toast.makeText(getContext(), 
                                    apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to delete review", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (response.code() == 401) {
                                Toast.makeText(getContext(), "Please login to delete review", Toast.LENGTH_SHORT).show();
                            } else if (response.code() == 403) {
                                Toast.makeText(getContext(), "You can only delete your own reviews", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to delete review", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                        if (getContext() == null) return;
                        android.util.Log.e("BookDetailFragment", "Error deleting review: " + t.getMessage());
                        Toast.makeText(getContext(), "Network error, please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}

