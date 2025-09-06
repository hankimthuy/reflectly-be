package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "journal_entries")
@Getter
@Setter
@NoArgsConstructor
public class JournalEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mood mood;

    @Lob // Large Object, suitable for long text entries
    private String content;

    @ManyToMany(fetch = FetchType.LAZY)
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

    // Enum for Mood
    public enum Mood {
        VERY_HAPPY,
        HAPPY,
        NEUTRAL,
        SAD,
        VERY_SAD
    }
}