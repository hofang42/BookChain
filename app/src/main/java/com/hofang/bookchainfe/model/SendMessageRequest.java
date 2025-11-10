package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName;

public class SendMessageRequest {
    @SerializedName("content")
    private String content;
    
    @SerializedName("messageType")
    private String messageType;

    public SendMessageRequest(String content) {
        this.content = content;
        this.messageType = "text";
    }

    public SendMessageRequest(String content, String messageType) {
        this.content = content;
        this.messageType = messageType;
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
}

