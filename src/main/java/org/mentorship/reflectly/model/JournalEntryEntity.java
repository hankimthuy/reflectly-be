package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // Vẫn import để dùng cho AccessLevel cụ thể
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a Journal Entry entity following Rich Domain Model principles.
 * State changes are handled via specific business methods like updateDetails() and manageFactors().
 */
@Entity
@Table(name = "journal_entries", indexes = {
        @Index(name = "idx_user_entry_date", columnList = "user_id, entry_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA persistence framework
public class JournalEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter(AccessLevel.PACKAGE) // Chỉ cho phép UserEntity set trường này qua addJournalEntry()
    private UserEntity user;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mood mood;

    @Lob
    private String content;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "journal_entry_factors",
            joinColumns = @JoinColumn(name = "journal_entry_id"),
            inverseJoinColumns = @JoinColumn(name = "factor_id")
    )
    private Set<FactorEntity> factors = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Enum for Mood remains the same
    public enum Mood {
        VERY_HAPPY, HAPPY, NEUTRAL, SAD, VERY_SAD
    }

    // --- Constructor ---

    public JournalEntryEntity(UserEntity user, LocalDate entryDate, Mood mood, String content) {
        this.user = Objects.requireNonNull(user, "User cannot be null");
        this.entryDate = Objects.requireNonNull(entryDate, "Entry date cannot be null");
        this.mood = Objects.requireNonNull(mood, "Mood cannot be null");
        this.content = content;
    }

    // --- Business Logic Methods ---

    /**
     * Updates the core details of the journal entry.
     * Replaces individual setters for mood, date, and content to ensure consistency.
     *
     * @param newDate New date for the entry.
     * @param newMood New mood for the entry.
     * @param newContent New content for the entry.
     */
    public void updateDetails(LocalDate newDate, Mood newMood, String newContent) {
        this.entryDate = Objects.requireNonNull(newDate, "Entry date cannot be null");
        this.mood = Objects.requireNonNull(newMood, "Mood cannot be null");
        this.content = newContent;
    }

    /**
     * Associates a factor with this journal entry.
     * @param factor The factor to add.
     */
    public void addFactor(FactorEntity factor) {
        this.factors.add(factor);
        // If FactorEntity had a collection of entries (bidirectional), sync other side here:
        // factor.getJournalEntries().add(this);
    }

    /**
     * Removes a factor from this journal entry.
     * @param factor The factor to remove.
     */
    public void removeFactor(FactorEntity factor) {
        this.factors.remove(factor);
        // If FactorEntity had a collection of entries (bidirectional), sync other side here:
        // factor.getJournalEntries().remove(this);
    }

    /**
     * Returns an unmodifiable view of the factors set.
     * Prevents direct modification of the collection from outside services.
     */
    public Set<FactorEntity> getFactors() {
        return Collections.unmodifiableSet(factors);
    }

    // --- equals() and hashCode() ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JournalEntryEntity that = (JournalEntryEntity) o;
        // If id is null (transient state), objects are considered different unless they are the same instance.
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        // Use a constant hash code for transient entities, and id's hash code for persistent ones.
        return id != null ? id.hashCode() : 31;
    }
}