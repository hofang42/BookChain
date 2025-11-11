package com.hofang.bookchainfe.network;

import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.model.AuthResponse;
import com.hofang.bookchainfe.model.ChangePasswordRequest;
import com.hofang.bookchainfe.model.GoogleSignInRequest;
import com.hofang.bookchainfe.model.LoginRequest;
import com.hofang.bookchainfe.model.PasswordResetOTPResponse;
import com.hofang.bookchainfe.model.RegisterRequest;
import com.hofang.bookchainfe.model.ResetPasswordRequest;
import com.hofang.bookchainfe.model.SendOTPRequest;
import com.hofang.bookchainfe.model.UpdateProfileRequest;
import com.hofang.bookchainfe.model.UploadResponse;
import com.hofang.bookchainfe.model.UserProfileResponse;
import com.hofang.bookchainfe.model.VerifyOTPRequest;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface AuthApiService {
    
    @POST("api/auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest request);
    
    @POST("api/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);
    
    @POST("api/auth/send-verification-otp")
    Call<ApiResponse<String>> sendVerificationOTP(@Body SendOTPRequest request);
    
    @POST("api/auth/verify-email")
    Call<ApiResponse<AuthResponse>> verifyEmail(@Body VerifyOTPRequest request);
    
    @POST("api/auth/google")
    Call<ApiResponse<AuthResponse>> googleSignIn(@Body GoogleSignInRequest request);
    
    @POST("api/auth/send-password-reset-otp")
    Call<ApiResponse<String>> sendPasswordResetOTP(@Body SendOTPRequest request);
    
    @POST("api/auth/verify-password-reset-otp")
    Call<ApiResponse<PasswordResetOTPResponse>> verifyPasswordResetOTP(@Body VerifyOTPRequest request);
    
    @POST("api/auth/verify-otp")
    Call<ApiResponse<String>> verifyOtp(@Body VerifyOTPRequest request);
    
    @POST("api/auth/reset-password")
    Call<ApiResponse<AuthResponse>> resetPassword(@Body ResetPasswordRequest request);

    @GET("api/auth/me")
    Call<ApiResponse<UserProfileResponse>> getCurrentProfile(@Header("Authorization") String token);

    @PUT("api/auth/profile")
    Call<ApiResponse<UserProfileResponse>> updateProfile(
            @Header("Authorization") String token,
            @Body UpdateProfileRequest request
    );

    @PUT("api/auth/change-password")
    Call<ApiResponse<String>> changePassword(
            @Header("Authorization") String token,
            @Body ChangePasswordRequest request
    );

    @Multipart
    @POST("api/auth/avatar")
    Call<ApiResponse<UploadResponse>> uploadAvatar(
            @Header("Authorization") String token,
            @Part MultipartBody.Part avatar
    );
}
