package org.mentorship.reflectly.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/details")
    public Map<String, Object> getUserDetails(Authentication authentication) {
        // The 'authentication' object contains the validated JWT claims.
        // Get the principal, which is a Jwt object in this case.
        Jwt jwt = (Jwt) authentication.getPrincipal();

        // The claims from the JWT are available in a Map.
        return jwt.getClaims();
    }
}