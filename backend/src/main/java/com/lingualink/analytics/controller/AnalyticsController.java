package com.lingualink.analytics.controller;

import com.lingualink.analytics.dto.*;
import com.lingualink.analytics.service.AnalyticsService;
import com.lingualink.common.config.OpenApiConfig;
import com.lingualink.common.exception.AppException;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    @PostMapping("/events")
    @Operation(summary = "Track a learning analytics event", description = "Stores a raw course analytics event for the authenticated user.")
    public ResponseEntity<AnalyticsEventResponse> trackEvent(
            @Valid @RequestBody AnalyticsEventRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long userId = getCurrentUserId(currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(analyticsService.trackEvent(request, userId, isAdmin(currentUser)));
    }

    @GetMapping("/course/{id}/overview")
    @Operation(summary = "Get course analytics overview", description = "Returns creator-only course activity, completion, and time metrics.")
    public ResponseEntity<CourseAnalyticsOverviewResponse> getOverview(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long creatorId = getCurrentUserId(currentUser);
        return ResponseEntity.ok(analyticsService.getOverview(id, creatorId, isAdmin(currentUser)));
    }

    @GetMapping("/course/{id}/questions")
    @Operation(summary = "Get problematic questions", description = "Returns course questions ordered by highest answer error rate.")
    public ResponseEntity<List<QuestionAnalyticsResponse>> getQuestions(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long creatorId = getCurrentUserId(currentUser);
        return ResponseEntity.ok(analyticsService.getQuestionAnalytics(id, creatorId, isAdmin(currentUser)));
    }

    @GetMapping("/course/{id}/dropoff")
    @Operation(summary = "Get lesson drop-off analytics", description = "Returns lesson-level progression and stopping points for enrolled students.")
    public ResponseEntity<List<DropoffLessonAnalyticsResponse>> getDropoff(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long creatorId = getCurrentUserId(currentUser);
        return ResponseEntity.ok(analyticsService.getDropoff(id, creatorId, isAdmin(currentUser)));
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new AppException("User not found"));
        return user.getId();
    }

    private boolean isAdmin(UserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
