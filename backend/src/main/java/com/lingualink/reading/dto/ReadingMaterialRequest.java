package com.lingualink.reading.dto;

import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReadingMaterialRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @NotNull(message = "Language is required")
        CourseLanguage language,

        @NotNull(message = "Level is required")
        CourseLevel level,

        @NotBlank(message = "Content is required")
        String content
) {
}
