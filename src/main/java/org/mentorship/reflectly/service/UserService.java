// File: src/main/java/org/mentorship/reflectly/user/UserService.java
package org.mentorship.reflectly.service;

import org.mentorship.reflectly.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

}