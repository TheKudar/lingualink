package com.lingualink.analytics.dto;

public record LessonTimeAnalyticsResponse(
        Long lessonId,
        Long moduleId,
        String lessonTitle,
        Double averageTimeSpentSeconds,
        Long totalTimeSpentSeconds
) {
}
