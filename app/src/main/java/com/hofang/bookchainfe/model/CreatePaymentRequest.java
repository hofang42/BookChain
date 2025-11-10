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

    public CreatePaymentRequest(List<PaymentItem> items, float totalPrice, String deliveryAddress) {
        this.items = items;
        this.totalPrice = totalPrice;
        this.deliveryAddress = deliveryAddress;
    }

    // --- Inner static class cho Item ---
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