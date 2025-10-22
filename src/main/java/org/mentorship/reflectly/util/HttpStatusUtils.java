package org.mentorship.reflectly.util;

import org.springframework.http.HttpStatus;

/**
 * Utility class for HTTP status code operations.
 * Provides common methods for mapping error codes to HTTP status codes.
 */
public class HttpStatusUtils {

    /**
     * Map error codes to HTTP status codes.
     * This method can be used across different controllers and services.
     * 
     * @param errorCode The error code string
     * @return Corresponding HttpStatus
     */
    public static HttpStatus getHttpStatusFromErrorCode(String errorCode) {
        return switch (errorCode) {
            case "400" -> HttpStatus.BAD_REQUEST;
            case "401" -> HttpStatus.UNAUTHORIZED;
            case "403" -> HttpStatus.FORBIDDEN;
            case "404" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * Get a default HTTP status for unknown error codes.
     * 
     * @return HttpStatus.INTERNAL_SERVER_ERROR
     */
    public static HttpStatus getDefaultErrorStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
