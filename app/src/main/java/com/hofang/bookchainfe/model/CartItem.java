package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("bookId")
    private Book book;
    
    @SerializedName("quantity")
    private int quantity;
    
    private boolean isSelected = true; // For checkout selection

    public CartItem() {
    }

    public CartItem(Book book, int quantity) {
        this.book = book;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public double getSubtotal() {
        if (book != null) {
            return book.getFinalPrice() * quantity;
        }
        return 0;
    }
}
