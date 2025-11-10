package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BranchesResponse {
    @SerializedName("book")
    private BookInfo book;
    
    @SerializedName("userLocation")
    private UserLocation userLocation;
    
    @SerializedName("totalBranches")
    private int totalBranches;
    
    @SerializedName("branches")
    private List<Branch> branches;
    
    @SerializedName("message")
    private String message;
    
    // Constructors
    public BranchesResponse() {
    }
    
    // Getters and Setters
    public BookInfo getBook() {
        return book;
    }
    
    public void setBook(BookInfo book) {
        this.book = book;
    }
    
    public UserLocation getUserLocation() {
        return userLocation;
    }
    
    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }
    
    public int getTotalBranches() {
        return totalBranches;
    }
    
    public void setTotalBranches(int totalBranches) {
        this.totalBranches = totalBranches;
    }
    
    public List<Branch> getBranches() {
        return branches;
    }
    
    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    // Inner classes
    public static class BookInfo {
        @SerializedName("_id")
        private String id;
        
        @SerializedName("title")
        private String title;
        
        @SerializedName("author")
        private String author;
        
        @SerializedName("coverImage")
        private String coverImage;
        
        @SerializedName("price")
        private Double price;
        
        @SerializedName("discount")
        private Integer discount;
        
        public BookInfo() {
        }
        
        // Getters and Setters
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getAuthor() {
            return author;
        }
        
        public void setAuthor(String author) {
            this.author = author;
        }
        
        public String getCoverImage() {
            return coverImage;
        }
        
        public void setCoverImage(String coverImage) {
            this.coverImage = coverImage;
        }
        
        public Double getPrice() {
            return price;
        }
        
        public void setPrice(Double price) {
            this.price = price;
        }
        
        public Integer getDiscount() {
            return discount;
        }
        
        public void setDiscount(Integer discount) {
            this.discount = discount;
        }
    }
    
    public static class UserLocation {
        @SerializedName("lat")
        private double lat;
        
        @SerializedName("lng")
        private double lng;
        
        public UserLocation() {
        }
        
        public UserLocation(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
        
        // Getters and Setters
        public double getLat() {
            return lat;
        }
        
        public void setLat(double lat) {
            this.lat = lat;
        }
        
        public double getLng() {
            return lng;
        }
        
        public void setLng(double lng) {
            this.lng = lng;
        }
    }
}
