package com.lingualink.course.service;

import com.lingualink.common.exception.AppException;
import com.lingualink.course.dto.LessonCompletionResponse;
import com.lingualink.course.entity.Course;
import com.lingualink.course.entity.Enrollment;
import com.lingualink.course.entity.EnrollmentStatus;
import com.lingualink.course.entity.Lesson;
import com.lingualink.course.entity.LessonProgress;
import com.lingualink.course.repository.EnrollmentRepository;
import com.lingualink.course.repository.LessonProgressRepository;
import com.lingualink.course.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonProgressService {

    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;
    private final CourseProgressService courseProgressService;

    @Transactional
    public LessonCompletionResponse completeLesson(Long courseId, Long moduleId, Long lessonId, Long studentId) {
        Lesson lesson = lessonRepository.findByIdAndModuleIdAndModuleCourseId(lessonId, moduleId, courseId)
                .orElseThrow(() -> new AppException(
                        "Lesson not found with id: " + lessonId + " for module: " + moduleId + " and course: " + courseId
                ));

        Course course = lesson.getModule().getCourse();
        if (!course.getStatus().equals(com.lingualink.course.entity.CourseStatus.PUBLISHED)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this lesson");
        }

        Enrollment enrollment = enrollmentService.getTrackableEnrollment(studentId, course.getId());
        LessonProgress progress = lessonProgressRepository.findByStudentIdAndLessonId(studentId, lessonId)
                .orElseGet(() -> LessonProgress.builder()
                        .studentId(studentId)
                        .lessonId(lessonId)
                        .build());

        if (!Boolean.TRUE.equals(progress.getCompleted())) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            lessonProgressRepository.save(progress);
        }

        CourseProgressService.CourseProgressSnapshot snapshot =
                courseProgressService.calculateProgress(studentId, course.getId());

        if (snapshot.totalItems() > 0
                && snapshot.completedItems() >= snapshot.totalItems()
                && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(progress.getCompletedAt());
            enrollmentRepository.save(enrollment);
        }

        return new LessonCompletionResponse(
                lesson.getId(),
                lesson.getModule().getId(),
                course.getId(),
                Boolean.TRUE.equals(progress.getCompleted()),
                progress.getCompletedAt(),
                snapshot.totalLessons(),
                snapshot.completedLessons(),
                snapshot.totalExercises(),
                snapshot.completedExercises(),
                snapshot.exerciseAttempts(),
                snapshot.totalItems(),
                snapshot.completedItems(),
                snapshot.progressPercentage()
        );
    }
}
