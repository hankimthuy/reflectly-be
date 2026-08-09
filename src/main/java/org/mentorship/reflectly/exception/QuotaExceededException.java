package org.mentorship.reflectly.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user hits a hard usage quota (e.g. max conversations per account) put in place
 * during the pre-launch/alpha phase to bound Gemini API spend.
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class QuotaExceededException extends RuntimeException {
    public QuotaExceededException(String message) {
        super(message);
    }
}
