package com.lingualink.course.dto;

import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;
import com.lingualink.course.entity.EnrollmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EnrolledCourseResponse(
        Long enrollmentId,
        Long courseId,
        String title,
        String creatorName,
        String creatorAvatarUrl,
        CourseLanguage language,
        CourseLevel level,
        BigDecimal price,
        Double rating,
        Integer reviewsCount,
        Integer totalStudents,
        String coverImageUrl,
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
