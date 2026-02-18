package org.mentorship.reflectly.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.constants.ApiConstants;
import org.mentorship.reflectly.dto.ChangePasswordRequestDto;
import org.mentorship.reflectly.dto.UpdateProfileRequestDto;
import org.mentorship.reflectly.dto.UserProfileRecord;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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

    @Operation(
        summary = "Update display name",
        description = "Update the authenticated user's display name"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Profile updated successfully"),
        @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation failed"),
        @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Not authenticated")
    })
    @PutMapping("/profile")
    public ResponseEntity<UserProfileRecord> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDto request) {
        UserEntity updated = userService.updateProfile(request.getFullName());
        return ResponseEntity.ok(userService.toProfileRecord(updated));
    }

    @Operation(
        summary = "Change password",
        description = "Change the authenticated user's password. If the user has no password yet (Google-only account), currentPassword can be omitted."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Password changed successfully"),
        @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation failed"),
        @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Current password is incorrect or not authenticated")
    })
    @PutMapping("/password")
    public ResponseEntity<UserProfileRecord> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto request) {
        UserEntity updated = userService.changePassword(
                request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(userService.toProfileRecord(updated));
    }

    @Operation(
        summary = "Upload avatar",
        description = "Upload an avatar image file for the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Avatar uploaded successfully"),
        @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Invalid file"),
        @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Not authenticated")
    })
    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    public ResponseEntity<UserProfileRecord> uploadAvatar(
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Generate unique filename
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;

        // Save to uploads directory
        Path uploadDir = Paths.get("uploads", "avatars");
        Files.createDirectories(uploadDir);
        Path filePath = uploadDir.resolve(filename);
        file.transferTo(filePath.toFile());

        // Store relative URL path
        String avatarUrl = "/uploads/avatars/" + filename;
        UserEntity updated = userService.updateAvatar(avatarUrl);
        return ResponseEntity.ok(userService.toProfileRecord(updated));
    }

    private String getFileExtension(String filename) {
        if (filename == null) return ".png";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex) : ".png";
    }
}
