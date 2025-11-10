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

    // --- CÁC HẰNG SỐ API ---
    public static final String USERS = BASE_URL + "api/users";
    public static final String BOOKS = BASE_URL + "api/books";
    public static final String BRANCHES = BASE_URL + "api/branches";
    // -----------------------

    private static Retrofit retrofit;
    private static Context appContext;

    /**
     * Khởi tạo Retrofit với Context (để dùng được AuthInterceptor).
     * Hàm này PHẢI được gọi trong MainActivity.onCreate().
     */
    public static void init(Context context) {
        appContext = context.getApplicationContext();

        if (retrofit == null) {
            // --- Logging interceptor (hiển thị log request/response) ---
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // --- Auth interceptor (tự động gắn token vào header) ---
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor);

            if (appContext != null) {
                clientBuilder.addInterceptor(new AuthInterceptor(appContext));
            }

            OkHttpClient client = clientBuilder.build();

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

    // --- Các Service ---
    public static AuthApiService getAuthApiService() {
        return getRetrofit().create(AuthApiService.class);
    }

    public static BookApiService getBookApiService() {
        return getRetrofit().create(BookApiService.class);
    }

    public static CartApiService getCartApiService() {
        return getRetrofit().create(CartApiService.class);
    }

    public static PaymentApiService getPaymentApiService() {
        return getRetrofit().create(PaymentApiService.class);
    }

    public static ChatApiService getChatApiService() {
        return getRetrofit().create(ChatApiService.class);
    }

    public static AddressApiService getAddressApiService() {
        checkInit();
        return retrofit.create(AddressApiService.class);
    }

    public static ReviewApiService getReviewApiService() {
        checkInit();
        return retrofit.create(ReviewApiService.class);
    }

    /**
     * Trả về instance Retrofit (để tạo service tùy chỉnh nếu cần).
     */
    public static Retrofit getRetrofit() {
        checkInit();
        return retrofit;
    }
}
