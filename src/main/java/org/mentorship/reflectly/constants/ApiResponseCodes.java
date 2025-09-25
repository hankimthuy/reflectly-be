package org.mentorship.reflectly.constants;

/**
 * Common API response codes and messages for consistent API responses.
 * Centralized location for all HTTP status codes and error messages.
 */
public class ApiResponseCodes {
    
    // Success codes
    public static final String SUCCESS = "200";
    
    // Client error codes
    public static final String UNAUTHORIZED = "401";

    // Common messages
    public static final String UNAUTHORIZED_MESSAGE = "Unauthorized - Invalid or missing authentication token";

    // Authentication specific messages
    public static final String INVALID_GOOGLE_TOKEN = "Invalid or expired Google ID token";
    public static final String USER_PROFILE_RETRIEVED = "User profile retrieved successfully";

    // Journal specific messages
    public static final String JOURNAL_ENTRIES_RETRIEVED = "Journal entries retrieved successfully";

}