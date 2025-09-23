package org.mentorship.reflectly.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mentorship.reflectly.DTO.UserProfileResponseDto;
import org.mentorship.reflectly.constants.ApiResponseCodes;
import org.mentorship.reflectly.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and user management APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Get user profile", 
        description = "Get authenticated user's profile information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = ApiResponseCodes.SUCCESS, description = ApiResponseCodes.USER_PROFILE_RETRIEVED),
        @ApiResponse(responseCode = ApiResponseCodes.UNAUTHORIZED, description = ApiResponseCodes.INVALID_GOOGLE_TOKEN)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/get-user-profile")
    public ResponseEntity<UserProfileResponseDto> getUserProfile(Authentication authentication) {
        return ResponseEntity.ok(authService.getUserProfile(authentication));
    }
}