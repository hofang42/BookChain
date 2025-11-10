package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class ConversationResponse {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("userId")
    private UserInfo userId;
    
    @SerializedName("adminId")
    private UserInfo adminId;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("lastMessage")
    private String lastMessage;
    
    @SerializedName("lastMessageAt")
    private String lastMessageAt;
    
    @SerializedName("unreadCount")
    private int unreadCount;
    
    @SerializedName("unreadCountForUser")
    private int unreadCountForUser;
    
    @SerializedName("unreadCountForAdmin")
    private int unreadCountForAdmin;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("updatedAt")
    private String updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserInfo getUserId() {
        return userId;
    }

    public void setUserId(UserInfo userId) {
        this.userId = userId;
    }

    public UserInfo getAdminId() {
        return adminId;
    }

    public void setAdminId(UserInfo adminId) {
        this.adminId = adminId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(String lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public int getUnreadCountForUser() {
        return unreadCountForUser;
    }

    public void setUnreadCountForUser(int unreadCountForUser) {
        this.unreadCountForUser = unreadCountForUser;
    }

    public int getUnreadCountForAdmin() {
        return unreadCountForAdmin;
    }

    public void setUnreadCountForAdmin(int unreadCountForAdmin) {
        this.unreadCountForAdmin = unreadCountForAdmin;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}

