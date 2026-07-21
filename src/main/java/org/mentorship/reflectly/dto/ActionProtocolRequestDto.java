package org.mentorship.reflectly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionProtocolRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Trigger is required")
    @Size(max = 255, message = "Trigger must not exceed 255 characters")
    private String trigger;

    @NotBlank(message = "Script is required")
    @Size(max = 5000, message = "Script must not exceed 5000 characters")
    private String script;
}
