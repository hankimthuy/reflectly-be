package org.mentorship.reflectly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mentorship.reflectly.model.RelationshipType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonRequestDto {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Relationship type is required")
    private RelationshipType relationshipType;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
