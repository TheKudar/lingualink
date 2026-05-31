package com.lingualink.analytics.dto;

public record DropoffLessonAnalyticsResponse(
        Long lessonId,
        Long moduleId,
        String lessonTitle,
        int lessonOrder,
        long startedUsers,
        long completedUsers,
        long stoppedUsers,
        double stopRatePercent
) {
}
