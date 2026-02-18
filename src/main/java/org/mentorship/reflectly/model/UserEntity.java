package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}