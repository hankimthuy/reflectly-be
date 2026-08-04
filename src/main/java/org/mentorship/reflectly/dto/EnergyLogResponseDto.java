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
public class EnergyLogResponseDto {

    private String id;
    private String userId;
    private Integer level;
    private String contextTag;
    private String notes;
    private Instant loggedAt;
    private Instant createdAt;
}
