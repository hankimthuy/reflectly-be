package org.mentorship.reflectly.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserProfileRecord(
    @JsonProperty("id") String id,
    @JsonProperty("email") String email,
    @JsonProperty("fullName") String fullName,
    @JsonProperty("pictureUrl") String pictureUrl
) {
}