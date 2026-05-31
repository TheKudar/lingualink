package com.lingualink.analytics.dto;

import java.util.List;

public record CourseAnalyticsOverviewResponse(
        Long courseId,
        long dailyActiveUsers,
        long weeklyActiveUsers,
        long enrolledUsers,
        long completedUsers,
        double completionRatePercent,
        Double averageSessionDurationSeconds,
        Double averageTotalTimeSpentSecondsPerUser,
        Double averageTimeToCompleteSeconds,
        List<LessonTimeAnalyticsResponse> lessonTimes
) {
}
