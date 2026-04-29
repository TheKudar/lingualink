package com.lingualink.course.service;

import com.lingualink.common.exception.AppException;
import com.lingualink.course.dto.CourseProgressResponse;
import com.lingualink.course.dto.EnrolledCourseResponse;
import com.lingualink.course.entity.Course;
import com.lingualink.course.entity.CourseStatus;
import com.lingualink.course.entity.Enrollment;
import com.lingualink.course.entity.EnrollmentStatus;
import com.lingualink.course.repository.CourseRepository;
import com.lingualink.course.repository.EnrollmentRepository;
import com.lingualink.user.entity.User;
import com.lingualink.user.entity.UserRole;
import com.lingualink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private static final List<EnrollmentStatus> TRACKABLE_STATUSES =
            List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED);

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseProgressService courseProgressService;

    @Transactional
    public EnrolledCourseResponse enrollInCourse(Long courseId, Long studentId) {
        User student = getStudentUser(studentId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException("Course not found with id: " + courseId));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only published courses can be enrolled");
        }

        if (course.getCreatorId().equals(studentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot enroll in your own course");
        }

        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .studentId(student.getId())
                .courseId(course.getId())
                .status(EnrollmentStatus.ACTIVE)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        course.setTotalStudents(course.getTotalStudents() + 1);

        return toEnrolledCourseResponse(savedEnrollment, course, buildCreatorInfoMap(List.of(course)));
    }

    public Page<EnrolledCourseResponse> getMyCourses(Long studentId, Pageable pageable) {
        getStudentUser(studentId);

        Page<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(studentId, pageable);
        List<Long> courseIds = enrollments.getContent().stream()
                .map(Enrollment::getCourseId)
                .toList();

        Map<Long, Course> coursesById = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));

        Map<Long, CreatorInfo> creatorInfoByCourseId = buildCreatorInfoMap(coursesById.values().stream().toList());

        List<EnrolledCourseResponse> content = enrollments.getContent().stream()
                .map(enrollment -> {
                    Course course = coursesById.get(enrollment.getCourseId());
                    if (course == null) {
                        throw new AppException("Course not found with id: " + enrollment.getCourseId());
                    }
                    return toEnrolledCourseResponse(enrollment, course, creatorInfoByCourseId);
                })
                .toList();

        return new PageImpl<>(content, pageable, enrollments.getTotalElements());
    }

    public CourseProgressResponse getCourseProgress(Long courseId, Long studentId) {
        Enrollment enrollment = getTrackableEnrollment(studentId, courseId);
        CourseProgressService.CourseProgressSnapshot progress =
                courseProgressService.calculateProgress(studentId, courseId);

        return new CourseProgressResponse(
                courseId,
                enrollment.getStatus(),
                enrollment.getEnrolledAt(),
                enrollment.getCompletedAt(),
                progress.totalLessons(),
                progress.completedLessons(),
                progress.totalExercises(),
                progress.completedExercises(),
                progress.exerciseAttempts(),
                progress.totalItems(),
                progress.completedItems(),
                progress.progressPercentage()
        );
    }

    public Enrollment getTrackableEnrollment(Long studentId, Long courseId) {
        getStudentUser(studentId);

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not enrolled in this course"
                ));

        if (!TRACKABLE_STATUSES.contains(enrollment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This enrollment is not active");
        }

        return enrollment;
    }

    private User getStudentUser(Long studentId) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new AppException("User not found with id: " + studentId));

        if (user.getRole() != UserRole.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can perform this action");
        }

        return user;
    }

    private EnrolledCourseResponse toEnrolledCourseResponse(
            Enrollment enrollment,
            Course course,
            Map<Long, CreatorInfo> creatorInfoByCourseId
    ) {
        CourseProgressService.CourseProgressSnapshot progress =
                courseProgressService.calculateProgress(enrollment.getStudentId(), course.getId());
        CreatorInfo creatorInfo = creatorInfoByCourseId.getOrDefault(course.getId(), CreatorInfo.empty());

        return new EnrolledCourseResponse(
                enrollment.getId(),
                course.getId(),
                course.getTitle(),
                creatorInfo.name(),
                creatorInfo.avatarUrl(),
                course.getLanguage(),
                course.getLevel(),
                course.getPrice(),
                course.getRating(),
                course.getReviewsCount(),
                course.getTotalStudents(),
                course.getCoverImageUrl(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt(),
                enrollment.getCompletedAt(),
                progress.totalLessons(),
                progress.completedLessons(),
                progress.totalExercises(),
                progress.completedExercises(),
                progress.exerciseAttempts(),
                progress.totalItems(),
                progress.completedItems(),
                progress.progressPercentage()
        );
    }

    private Map<Long, CreatorInfo> buildCreatorInfoMap(List<Course> courses) {
        Map<Long, Long> creatorIdsByCourseId = courses.stream()
                .collect(Collectors.toMap(Course::getId, Course::getCreatorId));

        Map<Long, User> creatorsById = userRepository.findAllById(creatorIdsByCourseId.values()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return creatorIdsByCourseId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            User creator = creatorsById.get(entry.getValue());
                            if (creator == null) {
                                return CreatorInfo.empty();
                            }
                            return new CreatorInfo(
                                    creator.getFirstName() + " " + creator.getLastName(),
                                    creator.getAvatarUrl()
                            );
                        }
                ));
    }

    private record CreatorInfo(String name, String avatarUrl) {
        private static CreatorInfo empty() {
            return new CreatorInfo(null, null);
        }
    }
}
