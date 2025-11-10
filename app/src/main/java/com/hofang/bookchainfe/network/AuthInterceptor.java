package com.hofang.bookchainfe.network;

import android.content.Context;

import com.hofang.bookchainfe.utils.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor to add authentication token to API requests
 */
public class AuthInterceptor implements Interceptor {
    private TokenManager tokenManager;

    public AuthInterceptor(Context context) {
        this.tokenManager = new TokenManager(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Get token from TokenManager
        String token = tokenManager.getToken();
        
        // If token exists, add it to the request header
        if (token != null && !token.isEmpty()) {
            Request.Builder requestBuilder = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token);
            
            return chain.proceed(requestBuilder.build());
        }
        
        // If no token, proceed with original request
        return chain.proceed(originalRequest);
    }
}

