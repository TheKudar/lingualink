package com.lingualink.course.controller;

import com.lingualink.common.exception.AppException;
import com.lingualink.course.dto.ExerciseAnswerRequest;
import com.lingualink.course.dto.ExerciseAttemptResponse;
import com.lingualink.course.dto.ExerciseCreateRequest;
import com.lingualink.course.dto.ExerciseResponse;
import com.lingualink.course.service.ExerciseService;
import com.lingualink.user.entity.User;
import com.lingualink.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/modules/{moduleId}/lessons/{lessonId}/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ExerciseResponse> createExercise(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long lessonId,
            @Valid @RequestBody ExerciseCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Long currentUserId = getCurrentUserId(currentUser);
        ExerciseResponse response = exerciseService.createExercise(
                courseId,
                moduleId,
                lessonId,
                request,
                currentUserId,
                isAdmin(currentUser)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ExerciseResponse>> getExercises(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Long currentUserId = getCurrentUserId(currentUser);
        return ResponseEntity.ok(exerciseService.getExercises(courseId, moduleId, lessonId, currentUserId, isAdmin(currentUser)));
    }

    @GetMapping("/{exerciseId}")
    public ResponseEntity<ExerciseResponse> getExercise(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long lessonId,
            @PathVariable Long exerciseId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Long currentUserId = getCurrentUserId(currentUser);
        return ResponseEntity.ok(exerciseService.getExercise(courseId, moduleId, lessonId, exerciseId, currentUserId, isAdmin(currentUser)));
    }

    @PutMapping("/{exerciseId}")
    public ResponseEntity<ExerciseResponse> updateExercise(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long lessonId,
            @PathVariable Long exerciseId,
            @Valid @RequestBody ExerciseCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Long currentUserId = getCurrentUserId(currentUser);
        return ResponseEntity.ok(exerciseService.updateExercise(courseId, moduleId, lessonId, exerciseId, request, currentUserId, isAdmin(currentUser)));
    }

    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<Void> deleteExercise(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long lessonId,
            @PathVariable Long exerciseId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Long currentUserId = getCurrentUserId(currentUser);
        exerciseService.deleteExercise(courseId, moduleId, lessonId, exerciseId, currentUserId, isAdmin(currentUser));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{exerciseId}/submit")
    public ResponseEntity<ExerciseAttemptResponse> submitAnswer(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long lessonId,
            @PathVariable Long exerciseId,
            @Valid @RequestBody ExerciseAnswerRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Long studentId = getCurrentUserId(currentUser);
        return ResponseEntity.ok(exerciseService.submitAnswer(courseId, moduleId, lessonId, exerciseId, request, studentId));
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
