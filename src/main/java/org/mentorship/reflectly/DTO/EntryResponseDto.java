package org.mentorship.reflectly.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for returning entry data in API responses.
 * Contains all entry information including timestamps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryResponseDto {

    private String id;
    private String userId;
    private String title;
    private String reflection;
    private List<String> emotions;
    private List<String> activities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
