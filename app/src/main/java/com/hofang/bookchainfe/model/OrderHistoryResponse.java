package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderHistoryResponse {

    @SerializedName("orders")
    private List<Order> orders;

    public List<Order> getOrders() {
        return orders;
    }
}