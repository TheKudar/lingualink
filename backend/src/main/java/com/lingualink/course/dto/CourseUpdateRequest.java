package com.lingualink.course.dto;

import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;
import com.lingualink.course.entity.CourseStatus;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpdateRequest {
    @Size(max = 100)
    private String title;

    @Size(max = 1000)
    private String description;

    private CourseLanguage language;
    private CourseLevel level;

    @PositiveOrZero
    private BigDecimal price;

    private String coverImageUrl;  // Убедись, что имя именно такое
    private CourseStatus status;
}