package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {

    @SerializedName("_id")
    private String id;

    @SerializedName("orderCode")
    private long orderCode;

    @SerializedName("items")
    private List<OrderItem> items;

    @SerializedName("totalPrice")
    private double totalPrice;

    @SerializedName("status")
    private String status; // pending, confirmed, cancelled

    @SerializedName("deliveryAddress")
    private String deliveryAddress;

    @SerializedName("createdAt")
    private String createdAt; // Giữ dạng String để parse sau

    // Getters
    public String getId() { return id; }
    public long getOrderCode() { return orderCode; }
    public List<OrderItem> getItems() { return items; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getCreatedAt() { return createdAt; }
}