package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class MessageResponse {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("conversationId")
    private String conversationId;
    
    @SerializedName("senderId")
    private UserInfo senderId;
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("messageType")
    private String messageType;
    
    @SerializedName("isRead")
    private boolean isRead;
    
    @SerializedName("readAt")
    private String readAt;
    
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

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public UserInfo getSenderId() {
        return senderId;
    }

    public void setSenderId(UserInfo senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getReadAt() {
        return readAt;
    }

    public void setReadAt(String readAt) {
        this.readAt = readAt;
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

