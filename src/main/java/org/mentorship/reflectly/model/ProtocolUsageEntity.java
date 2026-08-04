package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

/**
 * A single logged use of an {@link ActionProtocolEntity}: when it was used and how
 * effective it was. Kept as its own append-only log so effectiveness-over-time can be
 * charted in a later phase without losing history.
 */
@Entity
@Table(name = "protocol_usages", indexes = {
        @Index(name = "idx_protocolusage_protocol", columnList = "protocolId"),
        @Index(name = "idx_protocolusage_user", columnList = "userId")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProtocolUsageEntity extends AuditableEntity {

    @Id
    private String id;

    @Column(name = "protocol_id", nullable = false, length = 36)
    private String protocolId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "effectiveness", nullable = false, length = 20)
    private EffectivenessLevel effectiveness;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "used_at", nullable = false)
    private Instant usedAt;

    public ProtocolUsageEntity(String id, String protocolId, String userId, EffectivenessLevel effectiveness, String note, Instant usedAt) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.protocolId = Objects.requireNonNull(protocolId, "Protocol ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.effectiveness = Objects.requireNonNull(effectiveness, "Effectiveness cannot be null");
        this.note = note;
        this.usedAt = usedAt != null ? usedAt : Instant.now();
    }
}
