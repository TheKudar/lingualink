package com.lingualink.course.controller;

import com.lingualink.common.config.OpenApiConfig;
import com.lingualink.common.exception.AppException;
import com.lingualink.course.dto.CourseProgressResponse;
import com.lingualink.course.dto.CourseCreateRequest;
import com.lingualink.course.dto.CourseRejectRequest;
import com.lingualink.course.dto.CourseResponse;
import com.lingualink.course.dto.CourseReviewCreateRequest;
import com.lingualink.course.dto.CourseReviewResponse;
import com.lingualink.course.dto.CourseSummaryResponse;
import com.lingualink.course.dto.CourseUpdateRequest;
import com.lingualink.course.dto.EnrolledCourseResponse;
import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;
import com.lingualink.course.entity.CourseStatus;
import com.lingualink.course.service.CourseReviewService;
import com.lingualink.course.service.CourseService;
import com.lingualink.course.service.EnrollmentService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class CourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final CourseReviewService courseReviewService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a course", description = "Creates a new course owned by the authenticated user.")
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        Long creatorId = getCurrentUserId(currentUser);
        CourseResponse response = courseService.createCourse(request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a course", description = "Returns course details when the authenticated user has access.")
    public ResponseEntity<CourseResponse> getCourse(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        CourseResponse response = courseService.getCourseById(id, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a course", description = "Updates course metadata for the course owner or an admin.")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        CourseResponse response = courseService.updateCourse(id, request, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a course cover", description = "Uploads or replaces a course cover image for the owner or an admin.")
    public ResponseEntity<CourseResponse> uploadCourseCover(
            @PathVariable Long id,
            @Parameter(description = "Cover image file") @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        CourseResponse response = courseService.uploadCoverImage(id, currentUserId, isAdmin, file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a course", description = "Deletes a course for the owner or an admin.")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        courseService.deleteCourse(id, currentUserId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/published")
    @Operation(summary = "List published courses", description = "Searches and filters published courses with pagination.")
    public ResponseEntity<Page<CourseSummaryResponse>> getPublishedCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CourseLanguage language,
            @RequestParam(required = false) CourseLevel level,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CourseSummaryResponse> courses = courseService.getPublishedCourses(
                keyword, language, level, minPrice, maxPrice, minRating, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/creator/{creatorId}")
    @Operation(summary = "List creator courses", description = "Returns courses created by the selected creator.")
    public ResponseEntity<Page<CourseSummaryResponse>> getCreatorCourses(
            @PathVariable Long creatorId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CourseSummaryResponse> courses = courseService.getCreatorCourses(creatorId, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/my-courses")
    @Operation(summary = "List my created courses", description = "Returns courses created by the authenticated user.")
    public ResponseEntity<Page<CourseSummaryResponse>> getMyCreatedCourses(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long creatorId = getCurrentUserId(currentUser);
        Page<CourseSummaryResponse> courses = courseService.getCreatorCourses(creatorId, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/moderation/pending")
    @Operation(summary = "List pending courses", description = "Admin-only endpoint for courses awaiting moderation.")
    public ResponseEntity<Page<CourseResponse>> getPendingReviewCourses(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        requireAdmin(currentUser);
        return ResponseEntity.ok(courseService.getPendingReviewCourses(pageable));
    }

    @PostMapping("/{id}/enroll")
    @Operation(summary = "Enroll in a course", description = "Enrolls the authenticated user as a student in the course.")
    public ResponseEntity<EnrolledCourseResponse> enrollInCourse(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        Long studentId = getCurrentUserId(currentUser);
        EnrolledCourseResponse response = enrollmentService.enrollInCourse(id, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-enrollments")
    @Operation(summary = "List my enrollments", description = "Returns courses the authenticated user is enrolled in.")
    public ResponseEntity<Page<EnrolledCourseResponse>> getMyEnrollments(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            @ParameterObject @PageableDefault(size = 20, sort = "enrolledAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long studentId = getCurrentUserId(currentUser);
        return ResponseEntity.ok(enrollmentService.getMyCourses(studentId, pageable));
    }

    @GetMapping("/{id}/progress")
    @Operation(summary = "Get course progress", description = "Returns progress for the authenticated student's enrollment.")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        Long studentId = getCurrentUserId(currentUser);
        return ResponseEntity.ok(enrollmentService.getCourseProgress(id, studentId));
    }

    @PostMapping("/{id}/submit-for-review")
    @Operation(summary = "Submit a course for review", description = "Moves a course into pending moderation review.")
    public ResponseEntity<CourseResponse> submitForReview(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long currentUserId = getCurrentUserId(currentUser);
        CourseUpdateRequest request = CourseUpdateRequest.builder()
                .status(CourseStatus.PENDING_REVIEW)
                .build();

        CourseResponse response = courseService.updateCourse(
                id, request, currentUserId, false);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a course", description = "Admin-only endpoint that publishes a reviewed course.")
    public ResponseEntity<CourseResponse> approveCourse(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        requireAdmin(currentUser);
        return ResponseEntity.ok(courseService.approveCourse(id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a course", description = "Admin-only endpoint that rejects a course with a moderation reason.")
    public ResponseEntity<CourseResponse> rejectCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRejectRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        requireAdmin(currentUser);
        return ResponseEntity.ok(courseService.rejectCourse(id, request.reason()));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive a course", description = "Archives a course for the owner or an admin.")
    public ResponseEntity<CourseResponse> archiveCourse(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        CourseUpdateRequest request = CourseUpdateRequest.builder()
                .status(CourseStatus.ARCHIVED)
                .build();

        CourseResponse response = courseService.updateCourse(
                id, request, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reviews")
    @Operation(summary = "Create a course review", description = "Creates a review for a course the authenticated student can review.")
    public ResponseEntity<CourseReviewResponse> createReview(
            @PathVariable Long id,
            @Valid @RequestBody CourseReviewCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {

        Long studentId = getCurrentUserId(currentUser);
        CourseReviewResponse response = courseReviewService.createReview(id, studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "List course reviews", description = "Returns visible reviews for a course with pagination.")
    public ResponseEntity<Page<CourseReviewResponse>> getCourseReviews(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        return ResponseEntity.ok(courseReviewService.getCourseReviews(id, pageable, currentUserId, isAdmin));
    }

    // Вспомогательные методы
    private Long getCurrentUserId(UserDetails userDetails) {
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new AppException("User not found"));
        return user.getId();
    }

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void requireAdmin(UserDetails userDetails) {
        if (!isAdmin(userDetails)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can perform this action");
        }
    }
}
