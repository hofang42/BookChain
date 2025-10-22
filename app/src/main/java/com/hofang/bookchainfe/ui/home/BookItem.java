package com.hofang.bookchainfe.ui.home;

public class BookItem {
    private int coverImageResId;
    private String category;
    private String title;
    private String author;
    private String price;
    private String discount;

    // Constructor ĐẦY ĐỦ (cho Best Deals)
    public BookItem(int coverImageResId, String category, String title, String author, String price, String discount) {
        this.coverImageResId = coverImageResId;
        this.category = category;
        this.title = title;
        this.author = author;
        this.price = price;
        this.discount = discount;
    }

    // Constructor cho sách CÓ GIÁ (Top, Latest)
    public BookItem(int coverImageResId, String category, String title, String author, String price) {
        this(coverImageResId, category, title, author, price, null);
    }

    // Constructor cho sách SẮP RA MẮT (Upcoming)
    public BookItem(int coverImageResId, String category, String title, String author) {
        this(coverImageResId, category, title, author, null, null);
    }

    // Getters
    public int getCoverImageResId() { return coverImageResId; }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPrice() { return price; }
    public String getDiscount() { return discount; }
}