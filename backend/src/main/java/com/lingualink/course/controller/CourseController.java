package com.lingualink.course.controller;

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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final CourseReviewService courseReviewService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        Long creatorId = getCurrentUserId(currentUser);
        CourseResponse response = courseService.createCourse(request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        CourseResponse response = courseService.getCourseById(id, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        CourseResponse response = courseService.updateCourse(id, request, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        Long currentUserId = getCurrentUserId(currentUser);
        boolean isAdmin = isAdmin(currentUser);
        courseService.deleteCourse(id, currentUserId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/published")
    public ResponseEntity<Page<CourseSummaryResponse>> getPublishedCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CourseLanguage language,
            @RequestParam(required = false) CourseLevel level,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CourseSummaryResponse> courses = courseService.getPublishedCourses(
                keyword, language, level, minPrice, maxPrice, minRating, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<Page<CourseSummaryResponse>> getCreatorCourses(
            @PathVariable Long creatorId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CourseSummaryResponse> courses = courseService.getCreatorCourses(creatorId, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/my-courses")
    public ResponseEntity<Page<CourseSummaryResponse>> getMyCreatedCourses(
            @AuthenticationPrincipal UserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long creatorId = getCurrentUserId(currentUser);
        Page<CourseSummaryResponse> courses = courseService.getCreatorCourses(creatorId, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/moderation/pending")
    public ResponseEntity<Page<CourseResponse>> getPendingReviewCourses(
            @AuthenticationPrincipal UserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        requireAdmin(currentUser);
        return ResponseEntity.ok(courseService.getPendingReviewCourses(pageable));
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<EnrolledCourseResponse> enrollInCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        Long studentId = getCurrentUserId(currentUser);
        EnrolledCourseResponse response = enrollmentService.enrollInCourse(id, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-enrollments")
    public ResponseEntity<Page<EnrolledCourseResponse>> getMyEnrollments(
            @AuthenticationPrincipal UserDetails currentUser,
            @PageableDefault(size = 20, sort = "enrolledAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long studentId = getCurrentUserId(currentUser);
        return ResponseEntity.ok(enrollmentService.getMyCourses(studentId, pageable));
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        Long studentId = getCurrentUserId(currentUser);
        return ResponseEntity.ok(enrollmentService.getCourseProgress(id, studentId));
    }

    @PostMapping("/{id}/submit-for-review")
    public ResponseEntity<CourseResponse> submitForReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long currentUserId = getCurrentUserId(currentUser);
        CourseUpdateRequest request = CourseUpdateRequest.builder()
                .status(CourseStatus.PENDING_REVIEW)
                .build();

        CourseResponse response = courseService.updateCourse(
                id, request, currentUserId, false);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<CourseResponse> approveCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        requireAdmin(currentUser);
        return ResponseEntity.ok(courseService.approveCourse(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<CourseResponse> rejectCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRejectRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        requireAdmin(currentUser);
        return ResponseEntity.ok(courseService.rejectCourse(id, request.reason()));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<CourseResponse> archiveCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

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
    public ResponseEntity<CourseReviewResponse> createReview(
            @PathVariable Long id,
            @Valid @RequestBody CourseReviewCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long studentId = getCurrentUserId(currentUser);
        CourseReviewResponse response = courseReviewService.createReview(id, studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<Page<CourseReviewResponse>> getCourseReviews(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

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
