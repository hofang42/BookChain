package com.hofang.bookchainfe.model;

public class ChatContext {
    private String screenName;
    private String bookId;
    private String bookTitle;
    
    public ChatContext() {
    }
    
    public ChatContext(String screenName) {
        this.screenName = screenName;
    }
    
    public ChatContext(String screenName, String bookId, String bookTitle) {
        this.screenName = screenName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
    }
    
    public String getScreenName() {
        return screenName;
    }
    
    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }
    
    public String getBookId() {
        return bookId;
    }
    
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
}
