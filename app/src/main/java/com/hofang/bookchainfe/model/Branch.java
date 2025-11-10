package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class Branch {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("city")
    private String city;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("openingHours")
    private String openingHours;
    
    @SerializedName("isActive")
    private boolean isActive;
    
    @SerializedName("location")
    private Location location;
    
    @SerializedName("quantity")
    private Integer quantity; // Available quantity for specific book
    
    @SerializedName("distance")
    private Double distance; // Distance from user location in km
    
    // Constructors
    public Branch() {
    }
    
    public Branch(String id, String name, String address, String city, String phone, 
                  String openingHours, Location location) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.phone = phone;
        this.openingHours = openingHours;
        this.location = location;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getOpeningHours() {
        return openingHours;
    }
    
    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Double getDistance() {
        return distance;
    }
    
    public void setDistance(Double distance) {
        this.distance = distance;
    }
    
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null && !address.isEmpty()) {
            sb.append(address);
        }
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        return sb.toString();
    }
    
    // Inner class for Location
    public static class Location {
        @SerializedName("type")
        private String type;
        
        @SerializedName("coordinates")
        private double[] coordinates; // [longitude, latitude]
        
        public Location() {
        }
        
        public Location(double longitude, double latitude) {
            this.type = "Point";
            this.coordinates = new double[]{longitude, latitude};
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public double[] getCoordinates() {
            return coordinates;
        }
        
        public void setCoordinates(double[] coordinates) {
            this.coordinates = coordinates;
        }
        
        public double getLongitude() {
            return coordinates != null && coordinates.length >= 1 ? coordinates[0] : 0;
        }
        
        public double getLatitude() {
            return coordinates != null && coordinates.length >= 2 ? coordinates[1] : 0;
        }
    }
}
