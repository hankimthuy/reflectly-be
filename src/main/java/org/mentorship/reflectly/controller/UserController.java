package org.mentorship.reflectly.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.DTO.UserProfileResponseDto;
import org.mentorship.reflectly.constants.ApiResponseCodes;
import org.mentorship.reflectly.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management APIs")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "Get user profile", 
        description = "Get authenticated user's profile information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = ApiResponseCodes.SUCCESS, description = ApiResponseCodes.USER_PROFILE_RETRIEVED),
        @ApiResponse(responseCode = ApiResponseCodes.UNAUTHORIZED, description = ApiResponseCodes.INVALID_GOOGLE_TOKEN)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDto> getUserProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserProfile(authentication));
    }
}