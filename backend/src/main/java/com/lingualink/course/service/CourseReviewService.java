package com.lingualink.course.service;

import com.lingualink.common.exception.AppException;
import com.lingualink.course.dto.CourseReviewCreateRequest;
import com.lingualink.course.dto.CourseReviewResponse;
import com.lingualink.course.entity.Course;
import com.lingualink.course.entity.CourseReview;
import com.lingualink.course.entity.CourseStatus;
import com.lingualink.course.repository.CourseRepository;
import com.lingualink.course.repository.CourseReviewRepository;
import com.lingualink.user.entity.User;
import com.lingualink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseReviewService {

    private final CourseReviewRepository courseReviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;

    @Transactional
    public CourseReviewResponse createReview(Long courseId, Long studentId, CourseReviewCreateRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException("Course not found with id: " + courseId));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reviews are available only for published courses");
        }

        enrollmentService.getTrackableEnrollment(studentId, courseId);

        if (courseReviewRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already reviewed this course");
        }

        CourseReview review = CourseReview.builder()
                .courseId(courseId)
                .studentId(studentId)
                .rating(request.rating())
                .comment(normalizeComment(request.comment()))
                .build();

        CourseReview savedReview = courseReviewRepository.save(review);
        refreshCourseRating(course);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new AppException("User not found with id: " + studentId));

        return toResponse(savedReview, student);
    }

    public Page<CourseReviewResponse> getCourseReviews(Long courseId, Pageable pageable, Long currentUserId, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException("Course not found with id: " + courseId));

        if (course.getStatus() != CourseStatus.PUBLISHED && !isAdmin && !course.getCreatorId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to reviews for this course");
        }

        Page<CourseReview> reviews = courseReviewRepository.findByCourseIdOrderByCreatedAtDesc(courseId, pageable);
        Map<Long, User> studentsById = userRepository.findAllById(
                        reviews.getContent().stream().map(CourseReview::getStudentId).toList()
                ).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return reviews.map(review -> toResponse(review, studentsById.get(review.getStudentId())));
    }

    private void refreshCourseRating(Course course) {
        Double averageRating = courseReviewRepository.calculateAverageRating(course.getId());
        long reviewsCount = courseReviewRepository.countByCourseId(course.getId());

        course.setRating(averageRating != null ? averageRating : 0.0);
        course.setReviewsCount((int) reviewsCount);
    }

    private CourseReviewResponse toResponse(CourseReview review, User student) {
        return new CourseReviewResponse(
                review.getId(),
                review.getCourseId(),
                review.getStudentId(),
                student != null ? student.getUsername() : null,
                student != null ? student.getFirstName() : null,
                student != null ? student.getLastName() : null,
                student != null ? student.getAvatarUrl() : null,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }
        String trimmed = comment.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
