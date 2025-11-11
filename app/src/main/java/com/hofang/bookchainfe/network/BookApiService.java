// package com.hofang.bookchainfe.network;
package com.hofang.bookchainfe.network;

import com.hofang.bookchainfe.model.BranchesResponse;
import com.hofang.bookchainfe.model.UploadResponse;
import com.hofang.bookchainfe.ui.home.BookItem;
import com.hofang.bookchainfe.model.BookListResponse;
import com.hofang.bookchainfe.model.Category;


import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BookApiService {

    // (Các hàm getBestDeals, getTopBooks, getLatestBooks, getBookById, searchBooks giữ nguyên)
    // ...

    @GET("api/books/best-deals")
    Call<List<BookItem>> getBestDeals();

    @GET("api/books/top-books")
    Call<List<BookItem>> getTopBooks();

    @GET("api/books/latest-books")
    Call<List<BookItem>> getLatestBooks();

    @GET("api/books/{id}")
    Call<BookItem> getBookById(@Path("id") String bookId);

    @GET("api/books/search")
    Call<BookListResponse> searchBooks(@Query("q") String query);


    // --- BẮT ĐẦU SỬA ĐỔI ---
    @GET("api/books")
    Call<BookListResponse> getAllBooks(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("q") String query,
            @Query("sortBy") String sortBy,
            @Query("order") String order,
            @Query("categoryId") String categoryId // <-- THÊM THAM SỐ NÀY
    );

    // --- THÊM HÀM MỚI ---
    @GET("api/categories")
    Call<List<Category>> getAllCategories();
    // --- KẾT THÚC SỬA ĐỔI ---


    @Multipart
    @POST("api/books/upload")
    Call<UploadResponse> uploadBookCover(
            @Part MultipartBody.Part coverImage
    );

    @GET("api/books/{id}/branches")
    Call<BranchesResponse> getBranchesWithBook(
            @Path("id") String bookId,
            @Query("lat") Double lat,
            @Query("lng") Double lng,
            @Query("limit") Integer limit
    );
}