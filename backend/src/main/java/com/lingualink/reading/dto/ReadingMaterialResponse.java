package com.lingualink.reading.dto;

import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;

import java.time.LocalDateTime;

public record ReadingMaterialResponse(
        Long id,
        String title,
        CourseLanguage language,
        CourseLevel level,
        String content,
        Long creatorId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
