package com.hofang.bookchainfe.network;

import com.hofang.bookchainfe.model.UploadResponse;
import com.hofang.bookchainfe.ui.home.BookItem;
import com.hofang.bookchainfe.model.BookListResponse;


import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface BookApiService {

    // BASE_URL đã là "http://10.0.2.2:3000/"
    // Nên chúng ta chỉ cần đường dẫn tương đối

    @GET("api/books/best-deals")
    Call<List<BookItem>> getBestDeals();

    @GET("api/books/top-books")
    Call<List<BookItem>> getTopBooks();

    @GET("api/books/latest-books")
    Call<List<BookItem>> getLatestBooks();

    @GET("api/books/search")
    Call<BookListResponse> searchBooks(@Query("q") String query);

    @GET("api/books")
    Call<BookListResponse> getAllBooks(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("q") String query,
            @Query("sortBy") String sortBy,
            @Query("order") String order
    );

    @Multipart
    @POST("api/books/upload")
    Call<UploadResponse> uploadBookCover(
            @Part MultipartBody.Part coverImage
    );

    // (Bạn có thể thêm các API sách khác vào đây)
}