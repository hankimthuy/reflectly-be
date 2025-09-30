package org.mentorship.reflectly.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.DTO.UserProfileResponseDto;
import org.mentorship.reflectly.constants.ApiResponseCodes;
import org.mentorship.reflectly.security.GoogleAuthenticationToken;
import org.mentorship.reflectly.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDto> getUserProfile(GoogleAuthenticationToken authentication) {
        return ResponseEntity.ok(userService.getUserProfile(authentication));
    }
}