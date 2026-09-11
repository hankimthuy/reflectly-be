package org.mentorship.reflectly.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.constants.ApiConstants;
import org.mentorship.reflectly.dto.ChangePasswordRequestDto;
import org.mentorship.reflectly.dto.MoodSummaryResponseDto;
import org.mentorship.reflectly.dto.OnboardingRequestDto;
import org.mentorship.reflectly.dto.UpdateProfileRequestDto;
import org.mentorship.reflectly.dto.UserProfileRecord;
import org.mentorship.reflectly.dto.UserStatsResponseDto;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.security.GoogleAuthenticationToken;
import org.mentorship.reflectly.service.PersonService;
import org.mentorship.reflectly.service.UserService;
import org.mentorship.reflectly.service.UserStatsService;
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

    private static final int DEFAULT_MOOD_SUMMARY_DAYS = 7;
    private static final int MIN_MOOD_SUMMARY_DAYS = 1;
    private static final int MAX_MOOD_SUMMARY_DAYS = 90;

    private static final int DEFAULT_STATS_DAYS = 90;
    private static final int MIN_STATS_DAYS = 1;
    private static final int MAX_STATS_DAYS = 365;

    private final UserService userService;
    private final PersonService personService;
    private final UserStatsService userStatsService;

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

    @Operation(
        summary = "Complete onboarding",
        description = "Persist the user's core values and initial relationships (3-5 people), marking onboarding as complete"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Onboarding completed successfully"),
        @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation failed"),
        @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Not authenticated")
    })
    @PutMapping("/onboarding")
    public ResponseEntity<UserProfileRecord> completeOnboarding(
            @Valid @RequestBody OnboardingRequestDto request,
            GoogleAuthenticationToken authentication) {
        Long userId = authentication.getUser().getId();
        request.getPeople().forEach(person -> personService.createPerson(userId, person));
        UserEntity updated = userService.completeOnboarding(request.getCoreValues());
        return ResponseEntity.ok(userService.toProfileRecord(updated));
    }

    @Operation(
        summary = "Get day-bucketed mood summary",
        description = "One row per calendar day over the last N days (oldest first), each carrying the heaviest "
                + "mood signal that day — pooled across ended Coach sessions and written entries. Days with no "
                + "signal come back with hasData=false. Returns structured facts only; wording/trend copy is the "
                + "client's job."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Mood summary retrieved successfully"),
        @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Not authenticated")
    })
    @GetMapping("/mood-summary")
    public ResponseEntity<MoodSummaryResponseDto> getMoodSummary(
            @Parameter(description = "Number of days back, including today (default 7, clamped to 1-90)")
            @RequestParam(required = false) Integer days,
            @Parameter(description = "IANA timezone used to bucket days (default UTC; unrecognised values fall back to UTC)")
            @RequestParam(required = false) String tz,
            GoogleAuthenticationToken authentication) {
        Long userId = authentication.getUser().getId();
        int windowDays = clamp(days, DEFAULT_MOOD_SUMMARY_DAYS, MIN_MOOD_SUMMARY_DAYS, MAX_MOOD_SUMMARY_DAYS);
        return ResponseEntity.ok(userStatsService.getMoodSummary(userId, windowDays, tz));
    }

    @Operation(
        summary = "Get profile stats",
        description = "Writing streak (entries only, with a 1-day grace period), talk/entry totals, and the "
                + "emotion distribution over the last N days. The distribution always lists all 9 catalog "
                + "emotions, zero-filled, heaviest count first; mostFrequentEmotion is scoped to the same window."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Stats retrieved successfully"),
        @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Not authenticated")
    })
    @GetMapping("/stats")
    public ResponseEntity<UserStatsResponseDto> getStats(
            @Parameter(description = "Emotion window in days (default 90, clamped to 1-365)")
            @RequestParam(required = false) Integer days,
            @Parameter(description = "IANA timezone used to bucket days (default UTC; unrecognised values fall back to UTC)")
            @RequestParam(required = false) String tz,
            GoogleAuthenticationToken authentication) {
        Long userId = authentication.getUser().getId();
        int windowDays = clamp(days, DEFAULT_STATS_DAYS, MIN_STATS_DAYS, MAX_STATS_DAYS);
        return ResponseEntity.ok(userStatsService.getStats(userId, windowDays, tz));
    }

    private int clamp(Integer requested, int fallback, int min, int max) {
        if (requested == null) {
            return fallback;
        }
        return Math.min(Math.max(requested, min), max);
    }

    private String getFileExtension(String filename) {
        if (filename == null) return ".png";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex) : ".png";
    }
}
