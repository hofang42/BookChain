package com.hofang.bookchainfe.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class Review {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("userId")
    private Object userId; // Can be String (ID), User object, JsonObject, or LinkedTreeMap when populated
    
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
    
    // Populated user info (from userId when it's an object)
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
    
    public String getUserId() {
        if (userId instanceof String) {
            return (String) userId;
        } else if (userId instanceof User) {
            return ((User) userId).getId();
        } else if (userId instanceof JsonObject) {
            // Handle Gson JsonObject case
            JsonObject jsonObj = (JsonObject) userId;
            if (jsonObj.has("_id")) {
                return jsonObj.get("_id").getAsString();
            } else if (jsonObj.has("id")) {
                return jsonObj.get("id").getAsString();
            }
        } else if (userId instanceof Map) {
            // Handle LinkedTreeMap or other Map types
            Map<?, ?> map = (Map<?, ?>) userId;
            Object idObj = map.get("_id");
            if (idObj == null) {
                idObj = map.get("id");
            }
            if (idObj != null) {
                return idObj.toString();
            }
        }
        return null;
    }
    
    public void setUserId(Object userId) { 
        this.userId = userId;
        // If userId is a User object, also set it to user field
        if (userId instanceof User) {
            this.user = (User) userId;
        } else if (userId instanceof JsonObject) {
            // Parse JsonObject to User immediately when set
            try {
                JsonObject jsonObj = (JsonObject) userId;
                Gson gson = new Gson();
                this.user = gson.fromJson(jsonObj, User.class);
            } catch (Exception e) {
                android.util.Log.e("Review", "Error parsing userId JsonObject to User: " + e.getMessage());
            }
        } else if (userId instanceof Map) {
            // Handle LinkedTreeMap or other Map types from Gson deserialization
            try {
                Gson gson = new Gson();
                // Convert Map to JsonObject first, then parse to User
                JsonObject jsonObj = gson.toJsonTree(userId).getAsJsonObject();
                this.user = gson.fromJson(jsonObj, User.class);
            } catch (Exception e) {
                android.util.Log.e("Review", "Error parsing userId Map to User: " + e.getMessage());
            }
        }
    }
    
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
    
    public User getUser() {
        // If user is already set, return it
        if (user != null) {
            return user;
        }
        
        // If userId is a User object, return it
        if (userId instanceof User) {
            user = (User) userId;
            return user;
        }
        
        // If userId is a JsonObject, parse it to User
        if (userId instanceof JsonObject) {
            try {
                JsonObject jsonObj = (JsonObject) userId;
                Gson gson = new Gson();
                user = gson.fromJson(jsonObj, User.class);
                return user;
            } catch (Exception e) {
                android.util.Log.e("Review", "Error parsing userId JsonObject to User: " + e.getMessage());
            }
        }
        
        // If userId is a Map (LinkedTreeMap from Gson), parse it to User
        if (userId instanceof Map) {
            try {
                Gson gson = new Gson();
                // Convert Map to JsonObject first, then parse to User
                JsonObject jsonObj = gson.toJsonTree(userId).getAsJsonObject();
                user = gson.fromJson(jsonObj, User.class);
                return user;
            } catch (Exception e) {
                android.util.Log.e("Review", "Error parsing userId Map to User: " + e.getMessage());
            }
        }
        
        return null;
    }
    
    public void setUser(User user) { this.user = user; }
    
    public String getUserName() {
        User userObj = getUser();
        if (userObj != null) {
            return userObj.getFullName() != null ? userObj.getFullName() : userObj.getUsername();
        }
        return "Anonymous";
    }
}
