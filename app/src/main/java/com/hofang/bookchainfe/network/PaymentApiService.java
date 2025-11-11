package com.hofang.bookchainfe.network;

import com.hofang.bookchainfe.model.CancelOrderRequest;
import com.hofang.bookchainfe.model.CreatePaymentRequest;
import com.hofang.bookchainfe.model.CreatePaymentResponse;
import com.hofang.bookchainfe.model.OrderHistoryResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import okhttp3.ResponseBody;

public interface PaymentApiService {

    @POST("api/payments/create-link")
    Call<CreatePaymentResponse> createPaymentLink(@Body CreatePaymentRequest request);

    @POST("api/payments/cancel-order")
    Call<ResponseBody> cancelOrder(@Body CancelOrderRequest request);

    @GET("api/payments/my-orders")
    Call<OrderHistoryResponse> getUserOrders();

}