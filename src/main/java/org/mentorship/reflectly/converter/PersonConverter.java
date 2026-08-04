package org.mentorship.reflectly.converter;

import org.mentorship.reflectly.dto.PersonResponseDto;
import org.mentorship.reflectly.model.PersonEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class PersonConverter {

    private static final double RECENCY_DECAY_DAYS = 30.0;
    private static final long NUDGE_THRESHOLD_DAYS = 14;

    /**
     * @param recentSentimentAvg average of recent RelationshipEvent.sentimentScore for this
     *                           person (-1..1), or null if no events recorded yet.
     */
    public PersonResponseDto toResponseDto(PersonEntity entity, Double recentSentimentAvg) {
        if (entity == null) {
            return null;
        }

        Instant lastMentionedAt = entity.getLastMentionedAt();
        Long daysSinceLastMention = lastMentionedAt == null
                ? null
                : Duration.between(lastMentionedAt, Instant.now()).toDays();

        double healthSignal = computeHealthSignal(daysSinceLastMention, recentSentimentAvg);
        String nudgeText = computeNudgeText(entity.getName(), daysSinceLastMention);

        return PersonResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .relationshipType(entity.getRelationshipType())
                .notes(entity.getNotes())
                .lastMentionedAt(lastMentionedAt)
                .daysSinceLastMention(daysSinceLastMention)
                .healthSignal(healthSignal)
                .nudgeText(nudgeText)
                .build();
    }

    public List<PersonResponseDto> toResponseDtoList(List<PersonEntity> entities, java.util.Map<String, Double> sentimentByPersonId) {
        return entities.stream()
                .map(e -> toResponseDto(e, sentimentByPersonId.get(e.getId())))
                .toList();
    }

    private double computeHealthSignal(Long daysSinceLastMention, Double recentSentimentAvg) {
        if (daysSinceLastMention == null) {
            return 0.5;
        }
        double recencyScore = clamp(1.0 - (daysSinceLastMention / RECENCY_DECAY_DAYS), 0.0, 1.0);
        double sentimentComponent = recentSentimentAvg == null ? 0.5 : clamp((recentSentimentAvg + 1.0) / 2.0, 0.0, 1.0);
        return clamp(0.6 * recencyScore + 0.4 * sentimentComponent, 0.0, 1.0);
    }

    private String computeNudgeText(String name, Long daysSinceLastMention) {
        if (daysSinceLastMention == null) {
            return "Chưa có cập nhật nào về " + name + " — hãy chia sẻ khi bạn trò chuyện với Coach.";
        }
        if (daysSinceLastMention >= NUDGE_THRESHOLD_DAYS) {
            return "Bạn chưa nhắc đến " + name + " trong " + daysSinceLastMention + " ngày — có muốn ghi chú lại không?";
        }
        return null;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
