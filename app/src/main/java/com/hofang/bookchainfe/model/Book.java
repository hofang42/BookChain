package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class Book {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("author")
    private String author;
    
    @SerializedName("publisher")
    private String publisher;
    
    @SerializedName("publishedYear")
    private Integer publishedYear;
    
    @SerializedName("categoryId")
    private String categoryId;
    
    @SerializedName("isbn")
    private String isbn;
    
    @SerializedName("language")
    private String language;
    
    @SerializedName("price")
    private Double price;
    
    @SerializedName("discount")
    private Integer discount;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("coverImage")
    private String coverImage;
    
    @SerializedName("rating")
    private Double rating;
    
    @SerializedName("reviewCount")
    private Integer reviewCount;
    
    @SerializedName("stock")
    private Integer stock;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("updatedAt")
    private String updatedAt;
    
    // Category info (populated)
    @SerializedName("category")
    private Category category;
    
    public Book() {
    }
    
    public Book(String id, String title, String author, String categoryName, Double price, Integer discount, String description, String coverImage, Double rating) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.discount = discount;
        this.description = description;
        this.coverImage = coverImage;
        this.rating = rating;
        if (categoryName != null) {
            this.category = new Category();
            this.category.setName(categoryName);
        }
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    
    public Integer getPublishedYear() { return publishedYear; }
    public void setPublishedYear(Integer publishedYear) { this.publishedYear = publishedYear; }
    
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Integer getDiscount() { return discount; }
    public void setDiscount(Integer discount) { this.discount = discount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    
    public Double getRating() { return rating != null ? rating : 4.0; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    
    public Integer getStock() { return stock != null ? stock : 10; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public String getCategoryName() {
        if (category != null) {
            return category.getName();
        }
        return "Unknown";
    }
    
    public Double getFinalPrice() {
        if (discount != null && discount > 0) {
            return price * (100 - discount) / 100.0;
        }
        return price;
    }
}
