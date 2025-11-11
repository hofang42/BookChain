package com.hofang.bookchainfe.model;

public class ChatMessage {
    private String id;
    private String content;
    private String type; // "user" hoặc "ai"
    private long timestamp;
    private String imageBase64; // Nếu có ảnh
    
    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public ChatMessage(String content, String type) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.content = content;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }
    
    public ChatMessage(String content, String type, String imageBase64) {
        this(content, type);
        this.imageBase64 = imageBase64;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getImageBase64() {
        return imageBase64;
    }
    
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    
    public boolean isUser() {
        return "user".equals(type);
    }
    
    public boolean hasImage() {
        return imageBase64 != null && !imageBase64.isEmpty();
    }
}
