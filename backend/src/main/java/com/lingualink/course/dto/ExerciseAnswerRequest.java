package com.lingualink.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExerciseAnswerRequest(
        @NotBlank(message = "Answer is required")
        @Size(max = 1000, message = "Answer must be at most 1000 characters")
        String answer
) {
}
