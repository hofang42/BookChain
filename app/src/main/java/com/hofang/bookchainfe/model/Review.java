package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("userId")
    private String userId;
    
    @SerializedName("bookId")
    private String bookId;
    
    @SerializedName("rating")
    private Integer rating;
    
    @SerializedName("comment")
    private String comment;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("updatedAt")
    private String updatedAt;
    
    // Populated user info
    @SerializedName("user")
    private User user;
    
    public Review() {
    }
    
    public Review(String id, String userId, String bookId, Integer rating, String comment, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getUserName() {
        if (user != null) {
            return user.getFullName() != null ? user.getFullName() : user.getUsername();
        }
        return "Anonymous";
    }
}
