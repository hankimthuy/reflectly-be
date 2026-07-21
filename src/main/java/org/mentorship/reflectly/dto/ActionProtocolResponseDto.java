package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionProtocolResponseDto {

    private String id;
    private String userId;
    private String title;
    private String trigger;
    private String script;
    private Integer usageCount;
    private Instant lastUsedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
