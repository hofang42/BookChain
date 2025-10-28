package com.hofang.bookchainfe.model;

public class SendOTPRequest {
    private String email;

    public SendOTPRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
