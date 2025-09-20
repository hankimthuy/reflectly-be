package org.mentorship.reflectly.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for user profile response
 * Ensures consistent response structure for frontend
 */
public record UserProfileResponseDto(
    @JsonProperty("id") String id,
    @JsonProperty("email") String email,
    @JsonProperty("fullName") String fullName,
    @JsonProperty("pictureUrl") String pictureUrl,
    @JsonProperty("internalJwtToken") String internalJwtToken
) {
    public static UserProfileResponseDto of(String id, String email, String fullName, String pictureUrl, String internalJwtToken) {
        return new UserProfileResponseDto(
            id != null ? id : "",
            email != null ? email : "",
            fullName != null ? fullName : "",
            pictureUrl != null ? pictureUrl : "",
            internalJwtToken != null ? internalJwtToken : ""
        );
    }
}
