package com.lingualink.analytics.dto;

import com.lingualink.analytics.entity.AnalyticsEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record AnalyticsEventRequest(
        @NotNull @Positive Long courseId,
        @Positive Long lessonId,
        @Positive Long exerciseId,
        @NotNull AnalyticsEventType eventType,
        Boolean correct,
        @PositiveOrZero Long durationSeconds,
        String metadata
) {
}
