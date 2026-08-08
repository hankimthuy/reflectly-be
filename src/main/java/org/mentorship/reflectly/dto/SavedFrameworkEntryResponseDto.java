package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mentorship.reflectly.model.FrameworkType;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedFrameworkEntryResponseDto {

    private String id;
    private FrameworkType frameworkType;
    private String title;
    private Map<String, Object> payload;
    private String conversationId;
    private String personId;
    private String personName;
    private Instant createdAt;
    private Instant updatedAt;
}
