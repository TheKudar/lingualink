package com.lingualink.course.dto;

import com.lingualink.course.entity.ExerciseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ExerciseCreateRequest(
        @NotNull(message = "Exercise type is required")
        ExerciseType type,

        @NotBlank(message = "Question is required")
        @Size(max = 1000, message = "Question must be at most 1000 characters")
        String question,

        @Size(max = 20, message = "Options must contain at most 20 items")
        List<@Size(max = 500, message = "Each option must be at most 500 characters") String> options,

        @NotBlank(message = "Correct answer is required")
        @Size(max = 1000, message = "Correct answer must be at most 1000 characters")
        String correctAnswer,

        @Size(max = 1000, message = "Explanation must be at most 1000 characters")
        String explanation,

        @NotNull(message = "Order index is required")
        @PositiveOrZero(message = "Order index must be zero or positive")
        Integer orderIndex
) {
}
