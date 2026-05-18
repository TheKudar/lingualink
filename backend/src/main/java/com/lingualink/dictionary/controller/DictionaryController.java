package com.lingualink.dictionary.controller;

import com.lingualink.common.config.OpenApiConfig;
import com.lingualink.common.exception.AppException;
import com.lingualink.dictionary.dto.DictionaryCreateRequest;
import com.lingualink.dictionary.dto.DictionaryEntryRequest;
import com.lingualink.dictionary.dto.DictionaryResponse;
import com.lingualink.dictionary.service.DictionaryService;
import com.lingualink.user.entity.User;
import com.lingualink.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dictionaries")
@RequiredArgsConstructor
@Tag(name = "Dictionaries")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class DictionaryController {

    private final DictionaryService dictionaryService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "List my dictionaries", description = "Returns personal dictionaries for the authenticated user.")
    public ResponseEntity<List<DictionaryResponse>> list(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(dictionaryService.listDictionaries(getCurrentUserId(currentUser)));
    }

    @GetMapping("/{dictionaryId}")
    @Operation(summary = "Get dictionary", description = "Returns one personal dictionary with its entries.")
    public ResponseEntity<DictionaryResponse> get(
            @PathVariable Long dictionaryId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(dictionaryService.getDictionary(dictionaryId, getCurrentUserId(currentUser)));
    }

    @PostMapping
    @Operation(summary = "Create dictionary", description = "Creates a personal dictionary.")
    public ResponseEntity<DictionaryResponse> create(
            @Valid @RequestBody DictionaryCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dictionaryService.createDictionary(request, getCurrentUserId(currentUser)));
    }

    @PutMapping("/{dictionaryId}")
    @Operation(summary = "Rename dictionary", description = "Updates a personal dictionary name.")
    public ResponseEntity<DictionaryResponse> update(
            @PathVariable Long dictionaryId,
            @Valid @RequestBody DictionaryCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(dictionaryService.updateDictionary(dictionaryId, request, getCurrentUserId(currentUser)));
    }

    @DeleteMapping("/{dictionaryId}")
    @Operation(summary = "Delete dictionary", description = "Deletes a personal dictionary and all of its entries.")
    public ResponseEntity<Void> delete(
            @PathVariable Long dictionaryId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser
    ) {
        dictionaryService.deleteDictionary(dictionaryId, getCurrentUserId(currentUser));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{dictionaryId}/entries")
    @Operation(summary = "Add word pair", description = "Adds a word pair to a personal dictionary.")
    public ResponseEntity<DictionaryResponse> addEntry(
            @PathVariable Long dictionaryId,
            @Valid @RequestBody DictionaryEntryRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dictionaryService.addEntry(dictionaryId, request, getCurrentUserId(currentUser)));
    }

    @PutMapping("/{dictionaryId}/entries/{entryId}")
    @Operation(summary = "Edit word pair", description = "Updates a word pair in a personal dictionary.")
    public ResponseEntity<DictionaryResponse> updateEntry(
            @PathVariable Long dictionaryId,
            @PathVariable Long entryId,
            @Valid @RequestBody DictionaryEntryRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(dictionaryService.updateEntry(dictionaryId, entryId, request, getCurrentUserId(currentUser)));
    }

    @DeleteMapping("/{dictionaryId}/entries/{entryId}")
    @Operation(summary = "Delete word pair", description = "Deletes a word pair from a personal dictionary.")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable Long dictionaryId,
            @PathVariable Long entryId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser
    ) {
        dictionaryService.deleteEntry(dictionaryId, entryId, getCurrentUserId(currentUser));
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new AppException("User not found"));
        return user.getId();
    }
}
