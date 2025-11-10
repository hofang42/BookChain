package com.hofang.bookchainfe.model; // <-- Package mới (hoặc package bạn muốn)

import com.google.gson.annotations.SerializedName;
import com.hofang.bookchainfe.ui.home.BookItem; // <-- Import BookItem

import java.util.List;

public class BookListResponse {

    @SerializedName("data")
    private List<BookItem> data;

    public List<BookItem> getData() {
        return data;
    }

}