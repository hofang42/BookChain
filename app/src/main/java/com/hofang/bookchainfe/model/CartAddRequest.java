package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class CartAddRequest {

    @SerializedName("bookId")
    private String bookId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("branchId") // <-- THÊM MỚI
    private String branchId;

    // --- THAY ĐỔI CONSTRUCTOR ---
    public CartAddRequest(String bookId, int quantity, String branchId) {
        this.bookId = bookId;
        this.quantity = quantity;
        this.branchId = branchId;
    }

    // (Getters/Setters nếu bạn cần)
}