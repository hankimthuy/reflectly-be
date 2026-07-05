package org.mentorship.reflectly.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginRequestDto {

    @NotBlank(message = "Google Auth code is required")
    private String authCode;
}
