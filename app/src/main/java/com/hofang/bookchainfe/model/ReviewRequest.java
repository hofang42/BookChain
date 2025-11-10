package com.hofang.bookchainfe.model;

public class ReviewRequest {
    private String bookId;
    private Integer rating;
    private String comment;

    // Constructor for creating review (POST)
    public ReviewRequest(String bookId, Integer rating, String comment) {
        this.bookId = bookId;
        this.rating = rating;
        this.comment = comment;
    }

    // Constructor for updating review (PUT) - bookId not needed
    public ReviewRequest(Integer rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

