package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class CancelOrderRequest {
    @SerializedName("orderCode")
    private long orderCode;

    public CancelOrderRequest(long orderCode) {
        this.orderCode = orderCode;
    }

    public long getOrderCode() {
        return orderCode;
    }
}