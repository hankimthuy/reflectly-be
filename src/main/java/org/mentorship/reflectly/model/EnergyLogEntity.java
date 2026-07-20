package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single energy-level check-in (1-10 scale) tagged with a situational context.
 */
@Entity
@Table(name = "energy_logs", indexes = {
        @Index(name = "idx_energylog_user_loggedat", columnList = "userId, loggedAt")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnergyLogEntity extends AuditableEntity {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "context_tag", nullable = false, length = 50)
    private String contextTag;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "logged_at", nullable = false)
    private Instant loggedAt;

    public EnergyLogEntity(String id, String userId, Integer level, String contextTag, String notes, Instant loggedAt) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.level = Objects.requireNonNull(level, "Level cannot be null");
        this.contextTag = Objects.requireNonNull(contextTag, "Context tag cannot be null");
        this.notes = notes;
        this.loggedAt = loggedAt != null ? loggedAt : Instant.now();
    }
}
