package com.lingualink.analytics.dto;

public record QuestionAnalyticsResponse(
        Long questionId,
        Long lessonId,
        String question,
        long totalAnswers,
        long correctAnswers,
        long incorrectAnswers,
        double errorRatePercent
) {
}
