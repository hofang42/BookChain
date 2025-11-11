package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class QuantityUpdateRequest {

    @SerializedName("quantity")
    private int quantity;

    public QuantityUpdateRequest(int quantity) {
        this.quantity = quantity;
    }
}