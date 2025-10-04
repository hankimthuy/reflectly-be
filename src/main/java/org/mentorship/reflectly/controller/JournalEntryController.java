package org.mentorship.reflectly.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.constants.ApiResponseCodes;
import org.mentorship.reflectly.model.JournalEntryEntity;
import org.mentorship.reflectly.service.JournalEntryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/entries")
@Tag(name = "Journal Entries", description = "Journal entry management APIs")
@RequiredArgsConstructor
public class JournalEntryController {

    private final JournalEntryService journalEntryService;


    @Operation(
        summary = "Get all journal entries", 
        description = "Retrieve all journal entries for the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = ApiResponseCodes.SUCCESS, description = ApiResponseCodes.JOURNAL_ENTRIES_RETRIEVED),
        @ApiResponse(responseCode = ApiResponseCodes.UNAUTHORIZED, description = ApiResponseCodes.UNAUTHORIZED_MESSAGE)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public List<JournalEntryEntity> getAllJournalEntities() {
        return journalEntryService.getAllJournalEntities();
    }
}