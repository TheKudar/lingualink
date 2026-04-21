package com.lingualink.course.dto;

import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CourseSummaryResponse(
        Long id,
        String title,
        CourseLanguage language,
        CourseLevel level,
        BigDecimal price,
        Double rating,
        Integer reviewsCount,
        Integer totalStudents,
        String coverImageUrl,
        LocalDateTime createdAt
) {}
