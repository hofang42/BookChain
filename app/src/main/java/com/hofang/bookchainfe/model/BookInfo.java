package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class BookInfo implements Serializable {

    @SerializedName("title")
    private String title;

    @SerializedName("author")
    private String author;

    @SerializedName("price")
    private double price;

    // Getters
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public double getPrice() { return price; }
}