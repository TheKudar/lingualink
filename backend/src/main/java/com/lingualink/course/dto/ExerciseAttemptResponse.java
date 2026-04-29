package com.lingualink.course.dto;

import java.time.LocalDateTime;

public record ExerciseAttemptResponse(
        Long attemptId,
        Long exerciseId,
        Long courseId,
        String answer,
        boolean correct,
        String correctAnswer,
        String explanation,
        LocalDateTime attemptedAt,
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
