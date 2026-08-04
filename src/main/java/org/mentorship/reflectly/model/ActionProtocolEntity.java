package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

/**
 * A user-authored coping script ("Action Protocol") tied to a trigger/situation.
 * The user writes it in advance and, in the moment, follows it and marks it used
 * with a short effectiveness rating so the script can be refined over time.
 */
@Entity
@Table(name = "action_protocols", indexes = {
        @Index(name = "idx_actionprotocol_user", columnList = "userId")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActionProtocolEntity extends AuditableEntity {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "trigger_situation", nullable = false, length = 255)
    private String trigger;

    @Column(name = "script", nullable = false, columnDefinition = "TEXT")
    private String script;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    public ActionProtocolEntity(String id, String userId, String title, String trigger, String script) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.trigger = Objects.requireNonNull(trigger, "Trigger cannot be null");
        this.script = Objects.requireNonNull(script, "Script cannot be null");
        this.usageCount = 0;
    }

    /** Records a single use: bumps the counter and stamps the timestamp. */
    public void recordUsage(Instant usedAt) {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
        this.lastUsedAt = usedAt != null ? usedAt : Instant.now();
    }
}
