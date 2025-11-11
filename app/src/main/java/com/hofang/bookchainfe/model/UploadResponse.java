package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private boolean success;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}