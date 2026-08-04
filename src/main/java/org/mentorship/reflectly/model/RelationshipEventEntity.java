package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "relationship_events", indexes = {
        @Index(name = "idx_relationship_events_person", columnList = "person_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RelationshipEventEntity extends AuditableEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonEntity person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private ConversationEntity conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private RelationshipEventType eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    /** Range roughly -1.0 (very negative) to 1.0 (very positive); null if not determinable. */
    @Column(name = "sentiment_score")
    private Double sentimentScore;

    public RelationshipEventEntity(String id, PersonEntity person, ConversationEntity conversation,
                                    RelationshipEventType eventType, String summary, Double sentimentScore) {
        this.id = id;
        this.person = person;
        this.conversation = conversation;
        this.eventType = eventType;
        this.summary = summary;
        this.sentimentScore = sentimentScore;
    }
}
