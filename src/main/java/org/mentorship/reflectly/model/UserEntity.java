package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a User entity following Rich Domain Model principles.
 * Encapsulation is enforced by removing public setters and providing specific business methods
 * for state transitions. Relationships are carefully managed via helper methods.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 254)
    private String email;

    @Column(unique = true, length = 50)
    private String username;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "picture_url", nullable = false)
    private String pictureUrl;

    @Column(name = "password_hash")
    private String passwordHash;

    // EAGER: small list (a handful of strings), and UserEntity is often read from
    // GoogleAuthenticationToken outside the request's own transaction/session (e.g. auth
    // filter loaded it in a separate session) — LAZY here throws LazyInitializationException.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_core_values", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "value", length = 50)
    private List<String> coreValues = new ArrayList<>();

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted = false;
}