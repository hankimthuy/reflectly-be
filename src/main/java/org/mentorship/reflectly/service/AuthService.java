package org.mentorship.reflectly.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.mentorship.reflectly.DTO.AuthResponseDto;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.repository.UserRepository;
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

    @Transactional
    public AuthResponseDto processGoogleLogin(String googleIdTokenString) throws GeneralSecurityException, IOException {
        // 1. Validate Google ID Token
        GoogleIdToken idToken = googleVerifier.verify(googleIdTokenString);
        if (idToken == null) {
            throw new IllegalArgumentException("Invalid Google ID Token.");
        }
        GoogleIdToken.Payload payload = idToken.getPayload();

        // 2. Provision User: Find existing user or create a new one using rich model logic
        UserEntity user = findOrCreateUser(payload);

        // 3. Map to response DTO - No change here, getter still works
        AuthResponseDto.UserDto userDto = new AuthResponseDto.UserDto(
                user.getId().toString(),
                user.getEmail(),
                user.getPictureUrl(),
                user.getFullName()
        );

        return new AuthResponseDto(googleIdTokenString, userDto);
    }

    /**
     * Finds an existing user by email or creates a new one based on Google profile data.
     * Interacts with UserEntity using its business methods and rich constructor,
     * adhering to encapsulation principles.
     *
     * @param payload The decoded payload from the Google ID Token.
     * @return The persisted User entity.
     */
    private UserEntity findOrCreateUser(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        Optional<UserEntity> existingUserOpt = userRepository.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            // The user already exists. Update their profile information if necessary.
            // We call the business method 'updateProfile' instead of individual setters.
            UserEntity userToUpdate = existingUserOpt.get();
            String currentFullName = (String) payload.get("name");
            String currentPictureUrl = (String) payload.get("picture");

            userToUpdate.updateProfile(currentFullName, currentPictureUrl);

            return userRepository.save(userToUpdate);
        } else {
            // The user does not exist. Create a new record using the rich constructor.
            // This ensures the entity is created in a valid state according to its own rules.
            String fullName = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            UserEntity newUser = new UserEntity(email, fullName, pictureUrl);

            return userRepository.save(newUser);
        }
    }
}