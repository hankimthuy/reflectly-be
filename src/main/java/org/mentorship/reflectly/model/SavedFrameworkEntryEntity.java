package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * A user-authored, editable structured entry captured from (or inspired by) a Coach chat —
 * e.g. a Johari Window or a free-form log — shown and editable on "Đúc kết". Deliberately not
 * named Insight*: that name is already used by {@link InsightEntity}, the AI-derived, read-only
 * timeline shown on "Thấu hiểu". This entity is the opposite: user-owned and editable.
 * <p>
 * One table serves every {@link FrameworkType} via a JSONB {@code payload} rather than a table
 * per framework, so adding a new framework later is additive (new enum value + FE form), not a
 * migration.
 */
@Entity
@Table(name = "saved_framework_entries", indexes = {
        @Index(name = "idx_saved_framework_entries_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedFrameworkEntryEntity extends AuditableEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /** Which chat this was captured from, if any — free-standing entries are allowed too. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private ConversationEntity conversation;

    /** Which person (relationship map) this entry is about — used by LIFE_POSITIONS, which is
     * always about one specific relationship. Null for the "về bản thân" frameworks. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private PersonEntity person;

    @Enumerated(EnumType.STRING)
    @Column(name = "framework_type", nullable = false, length = 20)
    private FrameworkType frameworkType;

    @Column(length = 200)
    private String title;

    /**
     * Shape depends on {@link #frameworkType}: FREEFORM = {@code {content, tags[]}};
     * JOHARI_WINDOW = {@code {open, blind, hidden, unknown}}; ACT_MATRIX =
     * {@code {fiveSenses, values, awayMoves, towardMoves}}; PERSONAL_SWOT =
     * {@code {strengths, weaknesses, opportunities, threats}}; LIFE_POSITIONS =
     * {@code {position, notes}} (position is one of the LifePosition enum names — see FE model).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload;

    public SavedFrameworkEntryEntity(String id, UserEntity user, ConversationEntity conversation, PersonEntity person,
                                      FrameworkType frameworkType, String title, Map<String, Object> payload) {
        this.id = id;
        this.user = user;
        this.conversation = conversation;
        this.person = person;
        this.frameworkType = frameworkType;
        this.title = title;
        this.payload = payload;
    }
}
