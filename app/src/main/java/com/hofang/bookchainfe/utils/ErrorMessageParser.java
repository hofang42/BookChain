package com.hofang.bookchainfe.utils;

import android.util.Log;
import retrofit2.Response;

/**
 * Utility class for parsing error messages from API responses
 */
public class ErrorMessageParser {
    private static final String TAG = "ErrorMessageParser";
    
    /**
     * Parse error message from HTTP error response body
     * @param response The HTTP response
     * @param defaultMessage Default message to show if parsing fails
     * @return Parsed error message or default message
     */
    public static String parseErrorMessage(Response<?> response, String defaultMessage) {
        String errorMessage = defaultMessage;
        
        try {
            if (response.errorBody() != null) {
                String errorBodyString = response.errorBody().string();
                
                // Try to parse as JSON with "message" field
                if (errorBodyString.contains("\"message\"")) {
                    // Extract message from JSON response
                    int messageStart = errorBodyString.indexOf("\"message\":\"") + 11;
                    int messageEnd = errorBodyString.indexOf("\"", messageStart);
                    if (messageStart > 10 && messageEnd > messageStart) {
                        errorMessage = errorBodyString.substring(messageStart, messageEnd);
                    }
                }
                
                // Log the full error response for debugging
                Log.d(TAG, "Error response: " + errorBodyString);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error response", e);
        }
        
        return errorMessage;
    }
}
