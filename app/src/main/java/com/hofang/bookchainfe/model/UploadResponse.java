package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {

    // Giả sử API trả về một trường "message"
    @SerializedName("message")
    private String message;

    // Giả sử API trả về một trường "success"
    @SerializedName("success")
    private boolean success;

    // --- Getters ---

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    // --- Setters (Nếu cần) ---

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}