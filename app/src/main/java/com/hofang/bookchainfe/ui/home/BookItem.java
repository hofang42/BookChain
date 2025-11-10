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
    private double price; // Chuyển sang double
    private double discount; // Chuyển sang double
    private String coverImage; // Chuyển sang String (URL)

    // 'categoryId' trong JSON là một object, ta tạo một lớp con
    @SerializedName("categoryId")
    private Category category;

    // Lớp con để chứa thông tin category
    public static class Category {
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

    // --- Giữ lại constructor cũ cho "Upcoming Books" (vì API chưa có) ---
    // (Dùng để hiển thị dữ liệu hard-code cho "Upcoming")
    private int coverImageResId; // Chỉ dùng cho data hard-code

    public BookItem(int coverImageResId, String categoryName, String title, String author, double price) {
        this.coverImageResId = coverImageResId;
        this.category = new Category();
        this.category.name = categoryName;
        this.title = title;
        this.author = author;
        this.price = price;
        this.coverImage = null; // Đánh dấu đây là dữ liệu hard-code
    }
    public int getCoverImageResId() { return coverImageResId; }
    // --- Hết phần hard-code ---
}