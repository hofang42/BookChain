package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;
import com.hofang.bookchainfe.ui.home.BookItem;

import java.util.List;

public class BookListResponse {

    @SerializedName("data")
    private List<BookItem> data;

    // --- BẮT ĐẦU THÊM VÀO ---
    // Thêm các trường còn thiếu để khớp với JSON response

    @SerializedName("totalItems")
    private int totalItems;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("currentPage")
    private int currentPage;

    @SerializedName("itemsPerPage")
    private int itemsPerPage;

    // --- KẾT THÚC THÊM VÀO ---


    // --- Getters ---

    public List<BookItem> getData() {
        return data;
    }

    // --- THÊM GETTERS CHO CÁC TRƯỜNG MỚI ---

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        // Đây chính là hàm mà AllBooksFragment cần
        return totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }
}