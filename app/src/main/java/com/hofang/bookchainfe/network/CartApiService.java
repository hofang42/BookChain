package com.hofang.bookchainfe.network;

import com.hofang.bookchainfe.model.CartAddRequest;
import com.hofang.bookchainfe.model.CartResponse;
import com.hofang.bookchainfe.model.UploadResponse;
import com.hofang.bookchainfe.model.QuantityUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;


public interface CartApiService {
    @GET("api/cart")
    Call<CartResponse> getCart();

    @POST("api/cart")
    Call<UploadResponse> addToCart(@Body CartAddRequest request);

    @PUT("api/cart/{bookId}")
    Call<UploadResponse> updateCartItem(@Path("bookId") String bookId, @Body CartAddRequest request);

    @DELETE("api/cart/{bookId}")
    Call<UploadResponse> removeFromCart(@Path("bookId") String bookId);

    @PUT("api/cart/{bookId}")
    Call<UploadResponse> updateItemQuantity(
            @Path("bookId") String bookId,
            @Body QuantityUpdateRequest request
    );
}