package org.mentorship.reflectly.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.constants.ApiConstants;
import org.mentorship.reflectly.dto.UserProfileRecord;
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
        @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "User profile retrieved successfully"),
        @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or expired Google ID token")
    })
    @GetMapping("/profile")
    public ResponseEntity<UserProfileRecord> getUserProfile() {
        return ResponseEntity.ok(userService.getUserProfile());
    }
}
