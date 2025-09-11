package org.mentorship.reflectly.DTO;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for receiving the Google ID Token from the frontend client.
 * The frontend obtains this token upon successful authentication with Google.
 */

public record GoogleTokenDto(
        @NotBlank(message = "Google ID token cannot be blank")
        String idToken
) {}