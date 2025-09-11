package org.mentorship.reflectly.DTO;
/**
 * Data Transfer Object for sending authentication response back to the client.
 * Contains the application's internal JWT and user profile information.
 */
public record AuthResponseDto(
        String token, // Application's internal JWT
        UserDto user
) {

    public record UserDto(
            String id,
            String email,
            String picture,
            String fullName
    ) {}
}