package org.mentorship.reflectly.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mentorship.reflectly.model.FrameworkType;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedFrameworkEntryRequestDto {

    @NotNull(message = "Framework type is required")
    private FrameworkType frameworkType;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotEmpty(message = "Payload is required")
    private Map<String, Object> payload;

    /** Optional — which chat this was captured from, if any. */
    private String conversationId;

    /** Optional — which person (relationship map) this entry is about. Required in practice for
     * LIFE_POSITIONS, unused by the other framework types. */
    private String personId;
}
