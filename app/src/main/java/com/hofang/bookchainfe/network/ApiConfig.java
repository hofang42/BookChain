package com.hofang.bookchainfe.network;

/**
 * Central place for API base URL and endpoints.
 * Update BASE_URL here when switching between emulator, device or production.
 */
public class ApiConfig {
    // Default: Android emulator -> host machine
    public static final String BASE_URL = "http://10.0.2.2:3000";

    // Common endpoints
    public static final String USERS = BASE_URL + "/api/users";
    public static final String BOOKS = BASE_URL + "/api/books";
    public static final String BRANCHES = BASE_URL + "/api/branches";
}
