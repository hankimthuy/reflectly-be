package org.mentorship.reflectly.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mentorship.reflectly.model.EffectivenessLevel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkProtocolUsedRequestDto {

    @NotNull(message = "Effectiveness is required")
    private EffectivenessLevel effectiveness;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
