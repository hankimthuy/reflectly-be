package org.mentorship.reflectly.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when username/password login or signup is attempted while
 * {@code app.auth.local-credentials-enabled} is disabled (production: Google login only).
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class LocalAuthDisabledException extends RuntimeException {
    public LocalAuthDisabledException(String message) {
        super(message);
    }
}
