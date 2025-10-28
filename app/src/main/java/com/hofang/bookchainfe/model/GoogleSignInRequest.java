package com.hofang.bookchainfe.model;

/**
 * Request model for Google Sign-In API call
 */
public class GoogleSignInRequest {
    private String idToken;
    private String email;
    private String name;
    private String uid;

    public GoogleSignInRequest(String idToken, String email, String name, String uid) {
        this.idToken = idToken;
        this.email = email;
        this.name = name;
        this.uid = uid;
    }

    // Getters
    public String getIdToken() {
        return idToken;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    // Setters
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
