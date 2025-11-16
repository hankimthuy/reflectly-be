package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

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
    private Instant createdAt;
    private Instant updatedAt;
}
