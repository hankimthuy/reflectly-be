package org.mentorship.reflectly.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserProfileResponseDto(
    @JsonProperty("id") String id,
    @JsonProperty("email") String email,
    @JsonProperty("fullName") String fullName,
    @JsonProperty("pictureUrl") String pictureUrl
) {
}