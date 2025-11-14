package org.mentorship.reflectly.util;

import org.springframework.http.HttpStatus;

/** * Utility class for HTTP status code operations. */
public class HttpStatusUtils {

    public static HttpStatus getHttpStatusFromErrorCode(String errorCode) {
        return switch (errorCode) {
            case "400" -> HttpStatus.BAD_REQUEST;
            case "401" -> HttpStatus.UNAUTHORIZED;
            case "403" -> HttpStatus.FORBIDDEN;
            case "404" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    public static HttpStatus getDefaultErrorStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
