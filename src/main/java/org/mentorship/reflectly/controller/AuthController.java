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
import org.mentorship.reflectly.service.AuthService;
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
        description = "Exchange a Google ID token for a backend JWT and user profile"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Login successful"),
        @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Invalid Google ID token"),
        @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Google token verification failed")
    })
    @PostMapping("/google")
    public ResponseEntity<AuthLoginResponseDto> loginWithGoogle(
            @Valid @RequestBody AuthLoginRequestDto request) {
        AuthLoginResponseDto response = authService.loginWithGoogle(request.getIdToken());
        return ResponseEntity.ok(response);
    }
}
