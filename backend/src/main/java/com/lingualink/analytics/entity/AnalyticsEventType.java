package com.lingualink.analytics.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum AnalyticsEventType {
    COURSE_ENTER,
    COURSE_EXIT,
    LESSON_START,
    LESSON_COMPLETE,
    QUESTION_ANSWERED;

    @JsonCreator
    public static AnalyticsEventType fromValue(String value) {
        return AnalyticsEventType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
