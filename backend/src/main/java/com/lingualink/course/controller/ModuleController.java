package com.lingualink.course.controller;

import com.lingualink.common.config.OpenApiConfig;
import com.lingualink.common.exception.AppException;
import com.lingualink.course.dto.ModuleCreateRequest;
import com.lingualink.course.dto.ModuleResponse;
import com.lingualink.course.service.ModuleService;
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
@RequestMapping("/api/courses/{courseId}/modules")
@RequiredArgsConstructor
@Tag(name = "Modules")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ModuleController {

    private final ModuleService moduleService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a module", description = "Adds a module to a course for the owner or an admin.")
    public ResponseEntity<ModuleResponse> createModule(
            @PathVariable Long courseId,
            @Valid @RequestBody ModuleCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        ModuleResponse response = moduleService.createModule(courseId, request, currentUserId, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List course modules", description = "Returns modules for a course when the authenticated user has access.")
    public ResponseEntity<List<ModuleResponse>> getModules(
            @PathVariable Long courseId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        List<ModuleResponse> modules = moduleService.getModulesByCourse(courseId, currentUserId, isAdmin);
        return ResponseEntity.ok(modules);
    }

    @PutMapping("/{moduleId}")
    @Operation(summary = "Update a module", description = "Updates a course module for the owner or an admin.")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @Valid @RequestBody ModuleCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        ModuleResponse response = moduleService.updateModule(courseId, moduleId, request, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{moduleId}")
    @Operation(summary = "Delete a module", description = "Deletes a course module for the owner or an admin.")
    public ResponseEntity<Void> deleteModule(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        moduleService.deleteModule(courseId, moduleId, currentUserId, isAdmin);
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
