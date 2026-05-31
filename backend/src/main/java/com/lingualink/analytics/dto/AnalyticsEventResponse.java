package com.lingualink.analytics.dto;

import com.lingualink.analytics.entity.AnalyticsEventType;

import java.time.LocalDateTime;

public record AnalyticsEventResponse(
        Long id,
        Long userId,
        Long courseId,
        Long lessonId,
        Long exerciseId,
        AnalyticsEventType eventType,
        Boolean correct,
        Long durationSeconds,
        LocalDateTime occurredAt
) {
}
