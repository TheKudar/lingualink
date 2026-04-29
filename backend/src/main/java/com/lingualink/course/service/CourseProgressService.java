package com.lingualink.course.service;

import com.lingualink.course.repository.ExerciseAttemptRepository;
import com.lingualink.course.repository.ExerciseRepository;
import com.lingualink.course.repository.LessonProgressRepository;
import com.lingualink.course.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseProgressService {

    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseAttemptRepository exerciseAttemptRepository;

    public CourseProgressSnapshot calculateProgress(Long studentId, Long courseId) {
        long totalLessons = lessonRepository.countByModuleCourseId(courseId);
        long completedLessons = lessonProgressRepository.countCompletedLessonsForCourse(studentId, courseId);
        long totalExercises = exerciseRepository.countByLessonModuleCourseId(courseId);
        long completedExercises = exerciseAttemptRepository.countCompletedExercisesForCourse(studentId, courseId);
        long exerciseAttempts = exerciseAttemptRepository.countByStudentIdAndExerciseLessonModuleCourseId(studentId, courseId);
        long totalItems = totalLessons + totalExercises;
        long completedItems = completedLessons + completedExercises;
        int progressPercentage = totalItems == 0
                ? 0
                : Math.toIntExact((completedItems * 100) / totalItems);

        return new CourseProgressSnapshot(
                totalLessons,
                completedLessons,
                totalExercises,
                completedExercises,
                exerciseAttempts,
                totalItems,
                completedItems,
                progressPercentage
        );
    }

    public record CourseProgressSnapshot(
            long totalLessons,
            long completedLessons,
            long totalExercises,
            long completedExercises,
            long exerciseAttempts,
            long totalItems,
            long completedItems,
            int progressPercentage
    ) {
    }
}
