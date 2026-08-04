package org.mentorship.reflectly.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.constants.ApiConstants;
import org.mentorship.reflectly.dto.PersonRequestDto;
import org.mentorship.reflectly.dto.PersonResponseDto;
import org.mentorship.reflectly.security.GoogleAuthenticationToken;
import org.mentorship.reflectly.service.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/people")
@Tag(name = "People", description = "Relationship map — the people the user shares with their Coach")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @Operation(summary = "List people", description = "Get all people in the current user's relationship map, each with a computed health signal and optional nudge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "People retrieved successfully"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @GetMapping
    public ResponseEntity<List<PersonResponseDto>> getAllPeople(GoogleAuthenticationToken authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(personService.getAllPeople(userId));
    }

    @Operation(summary = "Get person by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Person retrieved successfully"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Person not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PersonResponseDto> getPersonById(
            @Parameter(description = "Person ID") @PathVariable String id,
            GoogleAuthenticationToken authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(personService.getPersonById(userId, id));
    }

    @Operation(summary = "Add a person", description = "Manually add a person to the relationship map (also used during onboarding)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.CREATED, description = "Person created successfully"),
            @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation error"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @PostMapping
    public ResponseEntity<PersonResponseDto> createPerson(
            @Valid @RequestBody PersonRequestDto requestDto,
            GoogleAuthenticationToken authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        PersonResponseDto person = personService.createPerson(userId, requestDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(person.getId())
                .toUri();
        return ResponseEntity.created(location).body(person);
    }

    @Operation(summary = "Update a person")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Person updated successfully"),
            @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation error"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Person not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PersonResponseDto> updatePerson(
            @Parameter(description = "Person ID") @PathVariable String id,
            @Valid @RequestBody PersonRequestDto requestDto,
            GoogleAuthenticationToken authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(personService.updatePerson(userId, id, requestDto));
    }

    private Long getUserIdFromAuthentication(GoogleAuthenticationToken authentication) {
        if (authentication != null && authentication.getUser() != null) {
            return authentication.getUser().getId();
        }
        throw new RuntimeException(ApiConstants.USER_NOT_AUTHENTICATED);
    }
}
