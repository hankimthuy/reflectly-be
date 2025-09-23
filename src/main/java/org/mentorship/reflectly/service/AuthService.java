package org.mentorship.reflectly.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.mentorship.reflectly.DTO.UserProfileResponseDto;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.repository.UserRepository;
import org.mentorship.reflectly.security.GoogleAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final GoogleIdTokenVerifier googleVerifier;

    public AuthService(UserRepository userRepository, GoogleIdTokenVerifier googleVerifier) {
        this.userRepository = userRepository;
        this.googleVerifier = googleVerifier;
    }

    /**
     * Find existing user by email or create a new one based on Google profile data
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
        } else {
            // Create new user
            String fullName = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            UserEntity newUser = new UserEntity(email, fullName, pictureUrl);
            return userRepository.save(newUser);
        }
    }

    /**
     * Validate Google ID token and return payload
     * @param googleIdTokenString The Google ID token
     * @return GoogleIdToken.Payload if valid
     * @throws GeneralSecurityException if token verification fails
     * @throws IOException if there's an I/O error
     */
    public GoogleIdToken.Payload validateGoogleToken(String googleIdTokenString) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = googleVerifier.verify(googleIdTokenString);
        if (idToken == null) {
            throw new IllegalArgumentException("Invalid Google ID Token.");
        }
        return idToken.getPayload();
    }

    /**
     * Get user profile from authentication context
     * @param authentication Spring Security authentication object
     * @return UserProfileResponseDto containing user information
     */
    public UserProfileResponseDto getUserProfile(Authentication authentication) {
        if (authentication instanceof GoogleAuthenticationToken googleAuth) {
            return UserProfileResponseDto.of(
                googleAuth.getUser().getId().toString(),
                googleAuth.getUser().getEmail(),
                googleAuth.getUser().getFullName(),
                googleAuth.getUser().getPictureUrl()
            );
        }
        
        return UserProfileResponseDto.of("", "", "", "");
    }
}