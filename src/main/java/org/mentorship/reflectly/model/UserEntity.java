package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(nullable = false)
    private String picture;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<JournalEntryEntity> journalEntries = new HashSet<>();
//
//    @CreationTimestamp
//    @Column(name = "created_at", updatable = false)
//    private Instant createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private Instant updatedAt;

}