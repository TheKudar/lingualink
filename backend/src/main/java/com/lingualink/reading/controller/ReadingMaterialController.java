package com.lingualink.reading.controller;

import com.lingualink.common.config.OpenApiConfig;
import com.lingualink.common.exception.AppException;
import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;
import com.lingualink.reading.dto.ReadingMaterialRequest;
import com.lingualink.reading.dto.ReadingMaterialResponse;
import com.lingualink.reading.service.ReadingMaterialService;
import com.lingualink.user.entity.User;
import com.lingualink.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reading-materials")
@RequiredArgsConstructor
@Tag(name = "Reading Materials")
public class ReadingMaterialController {

    private final ReadingMaterialService readingMaterialService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(
            summary = "Create reading material",
            description = "Creates a reading material entry owned by the authenticated user.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    public ResponseEntity<ReadingMaterialResponse> create(
            @Valid @RequestBody ReadingMaterialRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(readingMaterialService.create(request, getCurrentUserId(currentUser)));
    }

    @GetMapping
    @Operation(summary = "List reading materials", description = "Public endpoint for browsing reading materials by language and level.")
    public ResponseEntity<Page<ReadingMaterialResponse>> list(
            @RequestParam(required = false) CourseLanguage language,
            @RequestParam(required = false) CourseLevel level,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(readingMaterialService.list(language, level, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reading material", description = "Public endpoint that returns one reading material by id.")
    public ResponseEntity<ReadingMaterialResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(readingMaterialService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update reading material",
            description = "Updates reading material for the owner or an admin.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    public ResponseEntity<ReadingMaterialResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ReadingMaterialRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(readingMaterialService.update(id, request, getCurrentUserId(currentUser), isAdmin(currentUser)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete reading material",
            description = "Deletes reading material for the owner or an admin.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser
    ) {
        readingMaterialService.delete(id, getCurrentUserId(currentUser), isAdmin(currentUser));
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new AppException("User not found"));
        return user.getId();
    }

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
