package org.mentorship.reflectly.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Update payload for a saved framework entry — deliberately excludes frameworkType and
 * conversationId, which are immutable after creation (see SavedFrameworkEntryService.updateEntry).
 * Kept separate from SavedFrameworkEntryRequestDto (create) so its @NotNull frameworkType
 * validation doesn't apply here.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedFrameworkEntryUpdateRequestDto {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotEmpty(message = "Payload is required")
    private Map<String, Object> payload;

    /** Optional — which person (relationship map) this entry is about; editable, unlike
     * frameworkType/conversationId. Pass null/omit to clear it. */
    private String personId;
}
