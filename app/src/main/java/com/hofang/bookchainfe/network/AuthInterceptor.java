package com.hofang.bookchainfe.network;

import android.content.Context;
import androidx.annotation.NonNull;
import com.hofang.bookchainfe.utils.TokenManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor này sẽ tự động chặn mọi request API,
 * lấy token từ TokenManager (nếu có) và gắn vào header "Authorization".
 */
public class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;

    public AuthInterceptor(Context context) {
        // Khởi tạo TokenManager với Context được truyền vào
        this.tokenManager = new TokenManager(context);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder builder = originalRequest.newBuilder();

        // Lấy token hiện tại
        String token = tokenManager.getToken();

        // Nếu đã đăng nhập (có token), gắn nó vào header
        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        Request newRequest = builder.build();
        return chain.proceed(newRequest);
    }
}