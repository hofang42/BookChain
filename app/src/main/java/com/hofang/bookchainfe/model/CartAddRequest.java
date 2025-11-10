package com.hofang.bookchainfe.model;

public class CartAddRequest {
    private String bookId;
    private int quantity;

    public CartAddRequest(String bookId, int quantity) {
        this.bookId = bookId;
        this.quantity = quantity;
    }
}