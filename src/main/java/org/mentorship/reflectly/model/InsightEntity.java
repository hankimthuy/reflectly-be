package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "insights", indexes = {
        @Index(name = "idx_insights_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InsightEntity extends AuditableEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private ConversationEntity conversation;

    @Column(name = "insight_text", nullable = false, columnDefinition = "TEXT")
    private String insightText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InsightCategory category;

    public InsightEntity(String id, UserEntity user, ConversationEntity conversation,
                          String insightText, InsightCategory category) {
        this.id = id;
        this.user = user;
        this.conversation = conversation;
        this.insightText = insightText;
        this.category = category;
    }
}
