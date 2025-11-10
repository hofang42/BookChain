package com.hofang.bookchainfe.network;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hofang.bookchainfe.utils.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * AuthInterceptor — tự động chặn mọi request API,
 * lấy token từ TokenManager (nếu có) và gắn vào header "Authorization".
 */
public class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;

    public AuthInterceptor(Context context) {
        this.tokenManager = new TokenManager(context);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Lấy token từ TokenManager
        String token = tokenManager.getToken();

        // Nếu có token thì thêm vào header Authorization
        Request.Builder requestBuilder = originalRequest.newBuilder();
        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        Request newRequest = requestBuilder.build();
        return chain.proceed(newRequest);
    }
}
