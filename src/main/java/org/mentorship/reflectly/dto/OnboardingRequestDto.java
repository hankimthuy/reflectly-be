package org.mentorship.reflectly.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Both lists may be empty — onboarding is skippable per-step (values and/or people can be
 * set later from Profile), so this only validates shape, not presence.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequestDto {

    @NotNull
    private List<@NotEmpty String> coreValues;

    @NotNull
    @Size(max = 5, message = "Onboarding supports at most 5 relationships — more can be added later")
    private List<@Valid PersonRequestDto> people;
}
