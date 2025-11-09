package org.mentorship.reflectly.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
