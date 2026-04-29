package com.lingualink.course.dto;

import com.lingualink.course.entity.EnrollmentStatus;

import java.time.LocalDateTime;

public record CourseProgressResponse(
        Long courseId,
        EnrollmentStatus enrollmentStatus,
        LocalDateTime enrolledAt,
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
