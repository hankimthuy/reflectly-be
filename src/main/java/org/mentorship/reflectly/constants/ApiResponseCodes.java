package org.mentorship.reflectly.constants;

/**
 * Common API response codes and messages for consistent API responses.
 * Centralized location for all HTTP status codes and error messages.
 */
public class ApiResponseCodes {
    
    // Success codes
    public static final String SUCCESS = "200";
    public static final String CREATED = "201";
    public static final String NO_CONTENT = "204";
    
    // Client error codes
    public static final String BAD_REQUEST = "400";
    public static final String UNAUTHORIZED = "401";
    public static final String FORBIDDEN = "403";
    public static final String NOT_FOUND = "404";
    public static final String CONFLICT = "409";
    public static final String UNPROCESSABLE_ENTITY = "422";
    
    // Server error codes
    public static final String INTERNAL_SERVER_ERROR = "500";
    public static final String SERVICE_UNAVAILABLE = "503";
    
    // Common messages
    public static final String SUCCESS_MESSAGE = "Operation completed successfully";
    public static final String UNAUTHORIZED_MESSAGE = "Unauthorized - Invalid or missing authentication token";
    public static final String FORBIDDEN_MESSAGE = "Forbidden - Access denied";
    public static final String NOT_FOUND_MESSAGE = "Resource not found";
    public static final String BAD_REQUEST_MESSAGE = "Bad request - Invalid input data";
    public static final String INTERNAL_ERROR_MESSAGE = "Internal server error";
    
    // Authentication specific messages
    public static final String INVALID_GOOGLE_TOKEN = "Invalid or expired Google ID token";
    public static final String AUTHENTICATION_FAILED = "Authentication failed";
    public static final String INVALID_AUTHENTICATION_TYPE = "Invalid authentication type";
    public static final String USER_PROFILE_RETRIEVED = "User profile retrieved successfully";
    public static final String GOOGLE_AUTH_SUCCESS = "Google authentication successful";
    public static final String NO_VALID_AUTH = "No valid Google authentication found";
    
    // Journal specific messages
    public static final String JOURNAL_ENTRY_CREATED = "Journal entry created successfully";
    public static final String JOURNAL_ENTRY_UPDATED = "Journal entry updated successfully";
    public static final String JOURNAL_ENTRY_DELETED = "Journal entry deleted successfully";
    public static final String JOURNAL_ENTRY_NOT_FOUND = "Journal entry not found";
    public static final String JOURNAL_ENTRIES_RETRIEVED = "Journal entries retrieved successfully";
    
    // User specific messages
    public static final String USER_CREATED = "User created successfully";
    public static final String USER_UPDATED = "User updated successfully";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_ALREADY_EXISTS = "User already exists";
}
