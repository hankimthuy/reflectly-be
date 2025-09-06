// File: src/main/java/org/mentorship/reflectly/user/UserService.java
package org.mentorship.reflectly.service;

import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
}