package com.lingualink.report.controller;

import com.lingualink.common.config.OpenApiConfig;
import com.lingualink.common.exception.AppException;
import com.lingualink.report.dto.ReportCreateRequest;
import com.lingualink.report.dto.ReportResponse;
import com.lingualink.report.service.ReportService;
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
@RequestMapping({"/api/reports", "/reports"})
@RequiredArgsConstructor
@Tag(name = "Reports")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Report a course", description = "Creates a complaint about a course from the authenticated user.")
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody ReportCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long userId = getCurrentUserId(currentUser);
        ReportResponse response = reportService.createReport(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List reports", description = "Admin-only endpoint for all course complaints.")
    public ResponseEntity<List<ReportResponse>> getReports(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        requireAdmin(currentUser);
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @PostMapping("/{id}/ban-course")
    @Operation(summary = "Ban reported course", description = "Admin-only endpoint that archives the reported course.")
    public ResponseEntity<Void> banReportedCourse(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        requireAdmin(currentUser);
        reportService.banReportedCourse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/keep-course")
    @Operation(summary = "Keep reported course", description = "Admin-only endpoint that closes the report without changing the course.")
    public ResponseEntity<Void> keepReportedCourse(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        requireAdmin(currentUser);
        reportService.keepReportedCourse(id);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Требуется авторизация");
        }
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new AppException("Пользователь не найден"));
        return user.getId();
    }

    private boolean isAdmin(UserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void requireAdmin(UserDetails userDetails) {
        if (!isAdmin(userDetails)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Действие доступно только администратору");
        }
    }
}
