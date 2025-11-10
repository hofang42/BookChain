package com.hofang.bookchainfe.ui.message;

public class MessageItem {
    private String content;
    private String timestamp;
    private boolean isSent;

    public MessageItem(String content, String timestamp, boolean isSent) {
        this.content = content;
        this.timestamp = timestamp;
        this.isSent = isSent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }
}

