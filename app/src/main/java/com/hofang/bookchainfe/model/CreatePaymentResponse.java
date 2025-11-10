package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class CreatePaymentResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("checkoutUrl")
    private String checkoutUrl;

    @SerializedName("orderCode")
    private long orderCode;

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public String getMessage() {
        return message;
    }

    public long getOrderCode() {
        return orderCode;
    }
}