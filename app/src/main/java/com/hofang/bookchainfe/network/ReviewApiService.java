package com.hofang.bookchainfe.network;

import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.model.Review;
import com.hofang.bookchainfe.model.ReviewRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ReviewApiService {
    
    // GET /api/books/:bookId/reviews - Get all reviews for a book
    @GET("api/books/{bookId}/reviews")
    Call<ApiResponse<List<Review>>> getBookReviews(@Path("bookId") String bookId);
    
    // POST /api/reviews - Create a new review
    @POST("api/reviews")
    Call<ApiResponse<Review>> createReview(@Body ReviewRequest request);
    
    // PUT /api/reviews/:id - Update a review
    @PUT("api/reviews/{id}")
    Call<ApiResponse<Review>> updateReview(@Path("id") String reviewId, @Body ReviewRequest request);
    
    // DELETE /api/reviews/:id - Delete a review
    @DELETE("api/reviews/{id}")
    Call<ApiResponse<Void>> deleteReview(@Path("id") String reviewId);
}

