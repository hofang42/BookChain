package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("login")
    private String login; // Can be username or email
    
    @SerializedName("password")
    private String password;

    // Constructors
    public LoginRequest() {}

    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    // Getters and Setters
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
