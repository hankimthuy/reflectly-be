package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an Entry entity for the new entries system.
 * This entity follows the requirements for the new entries API with UUID-based IDs,
 * JSON arrays for emotions and activities, and proper timestamp management.
 */
@Entity
@Table(name = "entries", indexes = {
        @Index(name = "idx_user_entries", columnList = "userId"),
        @Index(name = "idx_entries_created_at", columnList = "createdAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntryEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Setter
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Setter
    @Column(name = "reflection", nullable = false, columnDefinition = "TEXT")
    private String reflection;

    @ElementCollection
    @CollectionTable(name = "entry_emotions", joinColumns = @JoinColumn(name = "entry_id"))
    @Column(name = "emotion", length = 50)
    private List<String> emotions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "entry_activities", joinColumns = @JoinColumn(name = "entry_id"))
    @Column(name = "activity", length = 100)
    private List<String> activities = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructor for creating new entries
    public EntryEntity(String id, String userId, String title, String reflection, List<String> emotions, List<String> activities) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.reflection = Objects.requireNonNull(reflection, "Reflection cannot be null");
        this.emotions = emotions != null ? new ArrayList<>(emotions) : new ArrayList<>();
        this.activities = activities != null ? new ArrayList<>(activities) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntryEntity that = (EntryEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 31;
    }
}
