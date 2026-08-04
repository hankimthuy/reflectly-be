package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mentorship.reflectly.model.EffectivenessLevel;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProtocolUsageResponseDto {

    private String id;
    private String protocolId;
    private EffectivenessLevel effectiveness;
    private String note;
    private Instant usedAt;
}
