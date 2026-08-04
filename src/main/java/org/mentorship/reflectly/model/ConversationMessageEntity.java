package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conversation_messages", indexes = {
        @Index(name = "idx_conversation_messages_conversation", columnList = "conversation_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationMessageEntity extends AuditableEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ConversationEntity conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role;

    /**
     * Encrypted at rest ({@link org.mentorship.reflectly.converter.EncryptedStringConverter}) and
     * nulled out after memory extraction succeeds (see app.conversations.purge-after-extraction).
     */
    @Convert(converter = org.mentorship.reflectly.converter.EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String content;

    public ConversationMessageEntity(String id, ConversationEntity conversation, MessageRole role, String content) {
        this.id = id;
        this.conversation = conversation;
        this.role = role;
        this.content = content;
    }
}
