package org.mentorship.reflectly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntryRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @NotBlank(message = "Reflection is required")
    @Size(max = 1000, message = "Reflection must not exceed 1000 characters")
    private String reflection;

    @NotEmpty(message = "At least one emotion is required")
    private List<String> emotions;

    private List<String> activities;
}
