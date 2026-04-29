package com.lingualink.course.dto;

import java.time.LocalDateTime;

public record CourseReviewResponse(
        Long id,
        Long courseId,
        Long studentId,
        String studentUsername,
        String studentFirstName,
        String studentLastName,
        String studentAvatarUrl,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}
