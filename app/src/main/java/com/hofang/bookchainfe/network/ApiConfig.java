package com.hofang.bookchainfe.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Central place for API configuration and Retrofit setup.
 * Update BASE_URL here when switching between emulator, device or production.
 */
public class ApiConfig {
    // Default: Android emulator -> host machine
    public static final String BASE_URL = "http://10.0.2.2:3000/";

    // Common endpoints
    public static final String USERS = BASE_URL + "api/users";
    public static final String BOOKS = BASE_URL + "api/books";
    public static final String BRANCHES = BASE_URL + "api/branches";

    private static Retrofit retrofit;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            // Create logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static AuthApiService getAuthApiService() {
        return getRetrofit().create(AuthApiService.class);
    }
}
