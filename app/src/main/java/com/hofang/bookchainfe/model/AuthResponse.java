package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("user")
    private User user;
    
    @SerializedName("token")
    private String token;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(User user, String token) {
        this.user = user;
        this.token = token;
    }

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
