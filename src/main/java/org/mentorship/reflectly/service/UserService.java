package org.mentorship.reflectly.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.dto.UserProfileRecord;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.repository.UserRepository;
import org.mentorship.reflectly.security.GoogleAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Find existing user by email or create a new one based on Google profile data
     *
     * @param payload The decoded payload from the Google ID Token
     * @return The persisted User entity
     */
    @Transactional
    public UserEntity findOrCreateUser(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        String fullName = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        Optional<UserEntity> existingUserOpt = userRepository.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            UserEntity userToUpdate = existingUserOpt.get();

            if (!Objects.equals(fullName, userToUpdate.getFullName()) || !Objects.equals(pictureUrl, userToUpdate.getPictureUrl())) {
                userToUpdate.setFullName(fullName);
                userToUpdate.setPictureUrl(pictureUrl != null ? pictureUrl : "");
                return userRepository.save(userToUpdate);
            }

            return userToUpdate;
        }

        UserEntity newUser = new UserEntity();
        newUser.setEmail(email);
        newUser.setFullName(fullName != null ? fullName : "");
        newUser.setPictureUrl(pictureUrl != null ? pictureUrl : "");
        return userRepository.save(newUser);
    }

    /**
     * Create a new user with username and password (credential-based signup).
     */
    @Transactional
    public UserEntity createUser(String username, String password, String fullName) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);
        newUser.setFullName(fullName != null ? fullName : "");
        newUser.setPictureUrl("");
        newUser.setPasswordHash(passwordEncoder.encode(password));
        return userRepository.save(newUser);
    }

    /**
     * Authenticate a user by username and password.
     *
     * @return the authenticated UserEntity
     * @throws IllegalArgumentException if credentials are invalid
     */
    public UserEntity authenticateByCredentials(String username, String password) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return user;
    }

    /**
     * Change the password for the currently authenticated user.
     */
    @Transactional
    public UserEntity changePassword(String currentPassword, String newPassword) {
        UserEntity user = getCurrentUserEntity();

        // If user already has a password, verify the current one
        if (user.getPasswordHash() != null) {
            if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    /**
     * Update the display name for the currently authenticated user.
     */
    @Transactional
    public UserEntity updateProfile(String fullName) {
        UserEntity user = getCurrentUserEntity();
        user.setFullName(fullName);
        return userRepository.save(user);
    }

    /**
     * Update the avatar URL for the currently authenticated user.
     */
    @Transactional
    public UserEntity updateAvatar(String pictureUrl) {
        UserEntity user = getCurrentUserEntity();
        user.setPictureUrl(pictureUrl);
        return userRepository.save(user);
    }

    /**
     * Get the currently authenticated UserEntity from the security context.
     */
    public UserEntity getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof GoogleAuthenticationToken googleAuth) {
            return googleAuth.getUser();
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found with email " + authentication.getName()));
    }

    public UserProfileRecord getUserProfile() {
        UserEntity entity = getCurrentUserEntity();
        return toProfileRecord(entity);
    }

    /**
     * Convert a UserEntity to a UserProfileRecord.
     */
    public UserProfileRecord toProfileRecord(UserEntity entity) {
        return new UserProfileRecord(
                entity.getId().toString(),
                entity.getEmail(),
                entity.getUsername(),
                entity.getFullName(),
                entity.getPictureUrl(),
                entity.getPasswordHash() != null
        );
    }
}