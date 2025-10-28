package com.hofang.bookchainfe.model;

public class PasswordResetOTPResponse {
    private String email;
    private boolean verified;

    public PasswordResetOTPResponse() {}

    public PasswordResetOTPResponse(String email, boolean verified) {
        this.email = email;
        this.verified = verified;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
