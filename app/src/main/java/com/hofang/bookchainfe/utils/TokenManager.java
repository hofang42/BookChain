package com.hofang.bookchainfe.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "BookChainPrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public TokenManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save user session
    public void saveUserSession(String token, String userId, String username, String email, String fullName) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    // Get token
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Get user info
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public String getFullName() {
        return sharedPreferences.getString(KEY_FULL_NAME, null);
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Clear session (logout)
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    // Check if token exists and is not empty
    public boolean hasValidToken() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }
}
