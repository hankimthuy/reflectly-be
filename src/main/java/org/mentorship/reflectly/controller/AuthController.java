package org.mentorship.reflectly.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.constants.ApiConstants;
import org.mentorship.reflectly.dto.AuthLoginRequestDto;
import org.mentorship.reflectly.dto.AuthLoginResponseDto;
import org.mentorship.reflectly.dto.CredentialLoginRequestDto;
import org.mentorship.reflectly.dto.SignupRequestDto;
import org.mentorship.reflectly.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication APIs")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Login with Google",
        description = "Exchange a Google authorization code for a backend JWT and user profile"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Login successful"),
        @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Invalid Google auth code"),
        @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Google token verification failed")
    })
    @PostMapping("/google")
    public ResponseEntity<AuthLoginResponseDto> loginWithGoogle(
            @Valid @RequestBody AuthLoginRequestDto request) {
        AuthLoginResponseDto response = authService.loginWithGoogle(request.getAuthCode());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Login with username and password",
        description = "Authenticate with username and password credentials and receive a backend JWT"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Login successful"),
        @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid username or password")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponseDto> loginWithCredentials(
            @Valid @RequestBody CredentialLoginRequestDto request) {
        AuthLoginResponseDto response = authService.loginWithCredentials(
                request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Register a new account",
        description = "Create a new user account with username and password"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = ApiConstants.CREATED, description = "Registration successful"),
        @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Username already taken or validation failed")
    })
    @PostMapping("/signup")
    public ResponseEntity<AuthLoginResponseDto> signup(
            @Valid @RequestBody SignupRequestDto request) {
        AuthLoginResponseDto response = authService.signup(
                request.getUsername(), request.getPassword(), request.getFullName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
