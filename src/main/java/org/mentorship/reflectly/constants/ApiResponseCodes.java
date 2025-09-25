package org.mentorship.reflectly.constants;

/**
 * Common API response codes and messages for consistent API responses.
 * Centralized location for all HTTP status codes and error messages.
 */
public class ApiResponseCodes {
    
    // Success codes
    public static final String SUCCESS = "200";
    
    // Client error codes
    public static final String BAD_REQUEST = "400";
    public static final String UNAUTHORIZED = "401";
    public static final String FORBIDDEN = "403";
    public static final String NOT_FOUND = "404";
    
    // Server error codes
    public static final String INTERNAL_SERVER_ERROR = "500";

    // Common messages
    public static final String UNAUTHORIZED_MESSAGE = "Unauthorized - Invalid or missing authentication token";
    public static final String INTERNAL_ERROR_MESSAGE = "Internal server error";

    // Authentication specific messages
    public static final String INVALID_GOOGLE_TOKEN = "Invalid or expired Google ID token";
    public static final String USER_PROFILE_RETRIEVED = "User profile retrieved successfully";

    // Journal specific messages
    public static final String JOURNAL_ENTRY_CREATED = "Journal entry created successfully";
    public static final String JOURNAL_ENTRY_UPDATED = "Journal entry updated successfully";
    public static final String JOURNAL_ENTRY_DELETED = "Journal entry deleted successfully";
    public static final String JOURNAL_ENTRY_NOT_FOUND = "Journal entry not found";
    public static final String JOURNAL_ENTRIES_RETRIEVED = "Journal entries retrieved successfully";

}