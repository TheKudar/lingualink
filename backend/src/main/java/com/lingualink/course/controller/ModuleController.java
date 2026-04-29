package com.lingualink.course.controller;

import com.lingualink.common.exception.AppException;
import com.lingualink.course.dto.ModuleCreateRequest;
import com.lingualink.course.dto.ModuleResponse;
import com.lingualink.course.service.ModuleService;
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
@RequestMapping("/api/courses/{courseId}/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ModuleResponse> createModule(
            @PathVariable Long courseId,
            @Valid @RequestBody ModuleCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        ModuleResponse response = moduleService.createModule(courseId, request, currentUserId, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ModuleResponse>> getModules(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        List<ModuleResponse> modules = moduleService.getModulesByCourse(courseId, currentUserId, isAdmin);
        return ResponseEntity.ok(modules);
    }

    @PutMapping("/{moduleId}")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @Valid @RequestBody ModuleCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        ModuleResponse response = moduleService.updateModule(courseId, moduleId, request, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{moduleId}")
    public ResponseEntity<Void> deleteModule(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @AuthenticationPrincipal UserDetails currentUser) {

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