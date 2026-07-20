package org.mentorship.reflectly.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnergyLogRequestDto {

    @NotNull(message = "Level is required")
    @Min(value = 1, message = "Level must be between 1 and 10")
    @Max(value = 10, message = "Level must be between 1 and 10")
    private Integer level;

    @NotBlank(message = "Context tag is required")
    @Size(max = 50, message = "Context tag must not exceed 50 characters")
    private String contextTag;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private Instant loggedAt;
}
