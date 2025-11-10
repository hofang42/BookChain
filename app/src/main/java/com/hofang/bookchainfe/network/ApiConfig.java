package com.hofang.bookchainfe.network;

import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cấu hình trung tâm cho API và Retrofit.
 * Cần được khởi tạo (init) một lần trong MainActivity.
 */
public class ApiConfig {
    // Default: Android emulator -> host machine
    public static final String BASE_URL = "http://10.0.2.2:3000/";

    // --- CÁC HẰNG SỐ BẠN CẦN (ĐÃ ĐƯỢC THÊM LẠI) ---
    public static final String USERS = BASE_URL + "api/users";
    public static final String BOOKS = BASE_URL + "api/books";
    public static final String BRANCHES = BASE_URL + "api/branches";
    // ----------------------------------------------

    private static Retrofit retrofit;

    /**
     * Khởi tạo Retrofit với Context (để dùng được AuthInterceptor).
     * Hàm này PHẢI được gọi trong MainActivity.onCreate().
     */
    public static void init(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Auth Interceptor (tự động gắn token)
            AuthInterceptor authInterceptor = new AuthInterceptor(context.getApplicationContext());

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(authInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
    }

    private static void checkInit() {
        if (retrofit == null) {
            throw new IllegalStateException("ApiConfig chưa được khởi tạo! Vui lòng gọi ApiConfig.init(getApplicationContext()) trong MainActivity.");
        }
    }

    public static AuthApiService getAuthApiService() {
        checkInit();
        return retrofit.create(AuthApiService.class);
    }

    public static BookApiService getBookApiService() {
        checkInit();
        return retrofit.create(BookApiService.class);
    }

    public static CartApiService getCartApiService() {
        checkInit();
        return retrofit.create(CartApiService.class);
    }

    public static PaymentApiService getPaymentApiService() {
        checkInit();
        return retrofit.create(PaymentApiService.class);
    }
}