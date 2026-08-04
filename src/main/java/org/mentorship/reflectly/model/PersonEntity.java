package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "people", indexes = {
        @Index(name = "idx_people_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonEntity extends AuditableEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 20)
    private RelationshipType relationshipType;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "last_mentioned_at")
    private Instant lastMentionedAt;

    public PersonEntity(String id, UserEntity user, String name, RelationshipType relationshipType, String notes) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.relationshipType = relationshipType;
        this.notes = notes;
    }
}
