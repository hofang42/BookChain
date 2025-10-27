package com.hofang.bookchainfe.network;

import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.model.AuthResponse;
import com.hofang.bookchainfe.model.LoginRequest;
import com.hofang.bookchainfe.model.RegisterRequest;
import com.hofang.bookchainfe.model.SendOTPRequest;
import com.hofang.bookchainfe.model.VerifyOTPRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    
    @POST("api/auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest request);
    
    @POST("api/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);
    
    @POST("api/auth/send-verification-otp")
    Call<ApiResponse<String>> sendVerificationOTP(@Body SendOTPRequest request);
    
    @POST("api/auth/verify-email")
    Call<ApiResponse<AuthResponse>> verifyEmail(@Body VerifyOTPRequest request);
}
