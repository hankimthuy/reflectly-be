package org.mentorship.reflectly.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;

import org.mentorship.reflectly.dto.UserProfileResponseDto;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.repository.UserRepository;
import org.mentorship.reflectly.security.GoogleAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Find existing user by email or create a new one based on Google profile data
     *
     * @param payload The decoded payload from the Google ID Token
     * @return The persisted User entity
     */
    @Transactional
    public UserEntity findOrCreateUser(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        Optional<UserEntity> existingUserOpt = userRepository.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            // Update existing user profile
            UserEntity userToUpdate = existingUserOpt.get();
            String currentFullName = (String) payload.get("name");
            String currentPictureUrl = (String) payload.get("picture");

            // Only update if new values are not null/empty
            if (currentFullName != null && !currentFullName.isEmpty()) {
                userToUpdate.updateProfile(currentFullName, currentPictureUrl);
            }

            return userRepository.save(userToUpdate);
        }
        // Create new user
        String fullName = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        UserEntity newUser = new UserEntity(email, fullName, pictureUrl);
        return userRepository.save(newUser);
    }

    /**
     * Get user profile from database based on authentication context
     *
     * @param authentication Spring Security authentication object
     * @return UserProfileResponseDto containing user information from database
     */
    public UserProfileResponseDto getUserProfile(GoogleAuthenticationToken authentication) {
        UserEntity user = authentication.getUser();
        return new UserProfileResponseDto(
                user.getId().toString(),
                user.getEmail(),
                user.getFullName(),
                user.getPictureUrl()
        );
    }
}