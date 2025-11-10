package com.hofang.bookchainfe.model;

import com.hofang.bookchainfe.ui.home.BookItem;
import java.util.List;
import java.io.Serializable;

public class CartResponse {
    private List<CartItem> items;
    private double totalPrice;

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public static class CartItem implements Serializable{
        private BookItem bookId; // IMPORTANT: MongoDB 'populate' often returns the object in the same field name
        private int quantity;
        private String _id;

        // Getter specifically for binding
        public BookItem getBook() {
            return bookId;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getId() {
            return _id;
        }
    }
}