package com.lingualink.course.controller;

import com.lingualink.common.config.OpenApiConfig;
import com.lingualink.common.exception.AppException;
import com.lingualink.course.dto.LessonCreateRequest;
import com.lingualink.course.dto.LessonCompletionResponse;
import com.lingualink.course.dto.LessonResponse;
import com.lingualink.course.service.LessonProgressService;
import com.lingualink.course.service.LessonService;
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

@RestController
@RequestMapping("/api/courses/{courseId}/modules/{moduleId}/lessons")
@RequiredArgsConstructor
@Tag(name = "Lessons")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class LessonController {

    private final LessonService lessonService;
    private final LessonProgressService lessonProgressService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a lesson", description = "Adds a lesson to a module for the course owner or an admin.")
    public ResponseEntity<LessonResponse> createLesson(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @Valid @RequestBody LessonCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        LessonResponse response = lessonService.createLesson(courseId, moduleId, request, currentUserId, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{lessonId}")
    @Operation(summary = "Get a lesson", description = "Returns a lesson when the authenticated user has course access.")
    public ResponseEntity<LessonResponse> getLesson(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long lessonId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        LessonResponse response = lessonService.getLesson(courseId, moduleId, lessonId, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{lessonId}")
    @Operation(summary = "Update a lesson", description = "Updates a lesson for the course owner or an admin.")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        LessonResponse response = lessonService.updateLesson(courseId, moduleId, lessonId, request, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{lessonId}")
    @Operation(summary = "Delete a lesson", description = "Deletes a lesson for the course owner or an admin.")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long lessonId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        lessonService.deleteLesson(courseId, moduleId, lessonId, currentUserId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{lessonId}/complete")
    @Operation(summary = "Complete a lesson", description = "Marks the lesson complete for the authenticated student.")
    public ResponseEntity<LessonCompletionResponse> completeLesson(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long lessonId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long studentId = getCurrentUserId(currentUser);
        LessonCompletionResponse response =
                lessonProgressService.completeLesson(courseId, moduleId, lessonId, studentId);
        return ResponseEntity.ok(response);
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
