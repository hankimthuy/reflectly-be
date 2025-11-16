package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an Entry entity for the new entries system.
 * This entity follows the requirements for the new entries API with UUID-based IDs,
 * JSON arrays for emotions, and proper timestamp management.
 */
@Entity
@Table(name = "entries", indexes = {
        @Index(name = "idx_user_entries", columnList = "userId"),
        @Index(name = "idx_entries_created_date", columnList = "createdDate")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntryEntity extends AuditableEntity {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "reflection", nullable = false, columnDefinition = "TEXT")
    private String reflection;

    @ElementCollection
    @CollectionTable(name = "entry_emotions", joinColumns = @JoinColumn(name = "entry_id"))
    @Column(name = "emotion", length = 50)
    private List<String> emotions = new ArrayList<>();

    // Constructor for creating new entries
    public EntryEntity(String id, String userId, String title, String reflection, List<String> emotions) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.reflection = Objects.requireNonNull(reflection, "Reflection cannot be null");
        this.emotions = emotions != null ? new ArrayList<>(emotions) : new ArrayList<>();
    }
}
