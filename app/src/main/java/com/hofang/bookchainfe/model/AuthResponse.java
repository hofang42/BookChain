package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("user")
    private User user;
    
    @SerializedName("token")
    private String token;
    
    @SerializedName("requiresVerification")
    private boolean requiresVerification;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("source")
    private String source;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public AuthResponse(String email, boolean requiresVerification, String source) {
        this.email = email;
        this.requiresVerification = requiresVerification;
        this.source = source;
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
    
    public boolean isRequiresVerification() {
        return requiresVerification;
    }
    
    public void setRequiresVerification(boolean requiresVerification) {
        this.requiresVerification = requiresVerification;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
}
