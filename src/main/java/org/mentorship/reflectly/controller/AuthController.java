package org.mentorship.reflectly.controller;

import org.mentorship.reflectly.DTO.AuthResponseDto;
import org.mentorship.reflectly.DTO.GoogleTokenDto;
import org.mentorship.reflectly.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/google-login")
    public ResponseEntity<AuthResponseDto> handleGoogleLogin(@RequestBody GoogleTokenDto tokenDto) {
        try {
            AuthResponseDto response = authService.processGoogleLogin(tokenDto.idToken());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            System.err.println("Verify token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (GeneralSecurityException | IOException e) {
            System.err.println("ERROR verify token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/get-user-profile")
    public Map<String, Object> getUserDetails(Authentication authentication) {
        // The 'authentication' object contains the validated JWT claims.
        // Get the principal, which is a Jwt object in this case.
        Jwt jwt = (Jwt) authentication.getPrincipal();

        // The claims from the JWT are available in a Map.
        return jwt.getClaims();
    }


}