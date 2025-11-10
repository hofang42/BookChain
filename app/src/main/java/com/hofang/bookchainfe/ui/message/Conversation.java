package com.hofang.bookchainfe.ui.message;

public class Conversation {
    private String id;
    private String name;
    private String lastMessage;
    private String timestamp;
    private boolean hasUnread;
    private int avatarResId; // For demo purposes, can be replaced with URL later

    public Conversation(String id, String name, String lastMessage, String timestamp, boolean hasUnread, int avatarResId) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.hasUnread = hasUnread;
        this.avatarResId = avatarResId;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean hasUnread() {
        return hasUnread;
    }

    public int getAvatarResId() {
        return avatarResId;
    }
}

