package com.lingualink.course.service;

import com.lingualink.course.entity.Course;
import com.lingualink.course.entity.CourseStatus;
import com.lingualink.course.entity.EnrollmentStatus;
import com.lingualink.course.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseAccessService {

    private static final List<EnrollmentStatus> CONTENT_ACCESS_STATUSES =
            List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED);

    private final EnrollmentRepository enrollmentRepository;

    public void validateCourseContentAccess(Course course, Long userId, boolean isAdmin) {
        if (!canAccessCourseContent(course, userId, isAdmin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this course content");
        }
    }

    public boolean canAccessCourseContent(Course course, Long userId, boolean isAdmin) {
        if (isAdmin) {
            return true;
        }
        if (Objects.equals(course.getCreatorId(), userId)) {
            return true;
        }
        return course.getStatus() == CourseStatus.PUBLISHED
                && enrollmentRepository.existsByStudentIdAndCourseIdAndStatusIn(
                userId,
                course.getId(),
                CONTENT_ACCESS_STATUSES
        );
    }
}
