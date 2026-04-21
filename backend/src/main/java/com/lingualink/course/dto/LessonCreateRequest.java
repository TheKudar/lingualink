package com.lingualink.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonCreateRequest {
    @NotBlank
    @Size(max = 100)
    private String title;

    private String content;

    @NotNull
    @PositiveOrZero
    private Integer orderIndex;
}