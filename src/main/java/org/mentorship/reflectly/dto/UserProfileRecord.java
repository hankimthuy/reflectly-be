package org.mentorship.reflectly.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record UserProfileRecord(
    @JsonProperty("id") String id,
    @JsonProperty("email") String email,
    @JsonProperty("username") String username,
    @JsonProperty("fullName") String fullName,
    @JsonProperty("pictureUrl") String pictureUrl,
    @JsonProperty("hasPassword") boolean hasPassword,
    @JsonProperty("coreValues") List<String> coreValues,
    @JsonProperty("onboardingCompleted") boolean onboardingCompleted
) {
}