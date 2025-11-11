package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class OrderItem implements Serializable {

    @SerializedName("bookId")
    private BookInfo bookInfo; // Dùng BookInfo để tránh nhầm lẫn với model Book đầy đủ

    @SerializedName("quantity")
    private int quantity;

    // Getters
    public BookInfo getBookInfo() { return bookInfo; }
    public int getQuantity() { return quantity; }
}