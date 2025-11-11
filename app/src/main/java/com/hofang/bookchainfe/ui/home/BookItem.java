package com.hofang.bookchainfe.ui.home;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.io.Serializable;

public class BookItem implements Serializable {

    // Các trường này phải khớp với JSON trả về từ API
    @SerializedName("_id")
    private String id;
    private String title;
    private String author;
    private double price;
    private double discount;
    private String coverImage;

    // --- THÊM CÁC TRƯỜNG BỊ THIẾU ---
    private String description;
    private double rating;
    // --- KẾT THÚC THÊM MỚI ---

    @SerializedName("categoryId")
    private Category category;

    // Lớp con để chứa thông tin category
    public static class Category implements Serializable { // Thêm Serializable ở đây cho an toàn
        private String name;
        public String getName() {
            return name;
        }
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public double getPrice() { return price; }
    public double getDiscount() { return discount; }
    public String getCoverImage() { return coverImage; }
    public Category getCategory() { return category; }

    // --- THÊM GETTERS CHO TRƯỜNG MỚI ---
    public String getDescription() { return description; }
    public double getRating() { return rating; }
    // --- KẾT THÚC THÊM MỚI ---


    // --- Constructor cho dữ liệu hard-code (Giữ nguyên) ---
    private int coverImageResId;
    public BookItem(int coverImageResId, String categoryName, String title, String author, double price) {
        this.coverImageResId = coverImageResId;
        this.category = new Category();
        this.category.name = categoryName;
        this.title = title;
        this.author = author;
        this.price = price;
        this.coverImage = null;
        this.description = "This is a great book. More details coming soon."; // Thêm mô tả mặc định
        this.rating = 4.0; // Thêm rating mặc định
    }
    public int getCoverImageResId() { return coverImageResId; }
    // --- Hết phần hard-code ---
}