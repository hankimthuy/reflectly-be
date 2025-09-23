package org.mentorship.reflectly.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserProfileResponseDto(
    @JsonProperty("id") String id,
    @JsonProperty("email") String email,
    @JsonProperty("fullName") String fullName,
    @JsonProperty("pictureUrl") String pictureUrl
) {
    public static UserProfileResponseDto of(String id, String email, String fullName, String pictureUrl) {
        return new UserProfileResponseDto(
            id != null ? id : "",
            email != null ? email : "",
            fullName != null ? fullName : "",
            pictureUrl != null ? pictureUrl : ""
        );
    }
}