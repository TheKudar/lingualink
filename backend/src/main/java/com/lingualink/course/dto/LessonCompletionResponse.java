package com.lingualink.course.dto;

import java.time.LocalDateTime;

public record LessonCompletionResponse(
        Long lessonId,
        Long moduleId,
        Long courseId,
        boolean completed,
        LocalDateTime completedAt,
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
