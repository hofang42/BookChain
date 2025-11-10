package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {
    @SerializedName("user")
    private User user;

    // Constructors
    public UserProfileResponse() {}

    public UserProfileResponse(User user) {
        this.user = user;
    }

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
