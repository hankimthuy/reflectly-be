package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_conversations_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationEntity extends AuditableEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    /**
     * AI-generated human-readable recap of the conversation (Vietnamese, markdown-formatted),
     * generated on demand via {@code POST /api/conversations/{id}/summarize} — not tied to
     * ending the session, and re-generatable. Encrypted at rest like message content, since it
     * can restate personal details from the conversation.
     */
    @Convert(converter = org.mentorship.reflectly.converter.EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String summary;

    public ConversationEntity(String id, UserEntity user) {
        this.id = id;
        this.user = user;
        this.status = ConversationStatus.ACTIVE;
        this.startedAt = Instant.now();
    }

    public void end() {
        this.status = ConversationStatus.ENDED;
        this.endedAt = Instant.now();
    }
}
