package org.mentorship.reflectly.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Represents a User entity following Rich Domain Model principles.
 * Encapsulation is enforced by removing public setters and providing specific business methods
 * for state transitions. Relationships are carefully managed via helper methods.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA requirement, protected access level
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "picture_url", nullable = false)
    private String pictureUrl;


    // --- Constructor ---

    public UserEntity(String email, String fullName, String pictureUrl) {
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.fullName = fullName;
        this.pictureUrl = pictureUrl;
    }

    /**
     * Updates user profile information based on new data.
     * Replaces individual setters like setFullName() and setPictureUrl().
     *
     * @param newFullName New full name.
     * @param newPictureUrl New picture URL.
     */
    public void updateProfile(String newFullName, String newPictureUrl) {
        this.fullName = newFullName;
        this.pictureUrl = newPictureUrl;
        // Add any validation logic here if needed.
    }

    // --- equals() and hashCode() based on business key ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        // Use business key for equality check. Avoid using generated 'id' if possible,
        // especially before the entity is persisted. Email must be non-null and unique.
        return email != null && email.equals(that.email);
    }

    @Override
    public int hashCode() {
        // Use business key for hash code generation.
        return Objects.hash(email);
    }
}