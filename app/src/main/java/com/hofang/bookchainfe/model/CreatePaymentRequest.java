package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CreatePaymentRequest {

    @SerializedName("items")
    private List<PaymentItem> items;

    @SerializedName("totalPrice")
    private float totalPrice;

    @SerializedName("deliveryAddress")
    private String deliveryAddress;

    // --- 1. THÊM TRƯỜNG branchId ---
    @SerializedName("branchId")
    private String branchId;

    // --- 2. CẬP NHẬT CONSTRUCTOR ---
    public CreatePaymentRequest(List<PaymentItem> items, float totalPrice, String deliveryAddress, String branchId) {
        this.items = items;
        this.totalPrice = totalPrice;
        this.deliveryAddress = deliveryAddress;
        this.branchId = branchId; // <-- Gán branchId
    }

    // --- Inner static class cho Item (Giữ nguyên) ---
    public static class PaymentItem {
        @SerializedName("bookId")
        private String bookId;

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("priceAtPurchase")
        private float priceAtPurchase;

        public PaymentItem(String bookId, int quantity, float priceAtPurchase) {
            this.bookId = bookId;
            this.quantity = quantity;
            this.priceAtPurchase = priceAtPurchase;
        }
    }
}