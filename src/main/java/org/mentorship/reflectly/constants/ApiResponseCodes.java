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

    // Entry specific codes
    public static final String ENTRY_NOT_FOUND = "404";
    public static final String ENTRY_FORBIDDEN = "403";
    public static final String VALIDATION_ERROR = "400";
    public static final String INTERNAL_SERVER_ERROR = "500";

    // Entry specific messages
    public static final String ENTRIES_RETRIEVED = "Entries retrieved successfully";
    public static final String ENTRY_RETRIEVED = "Entry retrieved successfully";
    public static final String ENTRY_CREATED = "Entry created successfully";
    public static final String ENTRY_UPDATED = "Entry updated successfully";
    public static final String ENTRY_DELETED = "Entry deleted successfully";
    public static final String ENTRY_NOT_FOUND_MESSAGE = "Entry not found";
    public static final String ENTRY_FORBIDDEN_MESSAGE = "Access denied - Entry does not belong to user";
    public static final String VALIDATION_ERROR_MESSAGE = "Validation failed";
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error occurred";

}