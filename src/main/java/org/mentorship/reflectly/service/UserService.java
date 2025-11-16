package org.mentorship.reflectly.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.dto.UserProfileRecord;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
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
        String fullName = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        Optional<UserEntity> existingUserOpt = userRepository.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            UserEntity userToUpdate = existingUserOpt.get();

            if (!Objects.equals(fullName, userToUpdate.getFullName()) || !Objects.equals(pictureUrl, userToUpdate.getPictureUrl())) {
                userToUpdate.setFullName(fullName);
                userToUpdate.setPictureUrl(pictureUrl);
                return userRepository.save(userToUpdate);
            }

            return userToUpdate;
        }

        UserEntity newUser = new UserEntity();
        newUser.setEmail(email);
        newUser.setFullName(fullName != null ? fullName : "");
        newUser.setPictureUrl(pictureUrl);
        return userRepository.save(newUser);
    }

    public UserProfileRecord getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByEmail(authentication.getName())
                .map(entity -> new UserProfileRecord(entity.getId().toString(), entity.getEmail(), entity.getFullName(), entity.getPictureUrl()))
                .orElseThrow(() -> new RuntimeException("User not found with email " + authentication.getName()));
    }
}