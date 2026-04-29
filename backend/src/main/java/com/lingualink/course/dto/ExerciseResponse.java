package com.lingualink.course.dto;

import com.lingualink.course.entity.ExerciseType;

public record ExerciseResponse(
        Long id,
        Long lessonId,
        ExerciseType type,
        String question,
        String options,
        String correctAnswer,
        String explanation,
        Integer orderIndex
) {
}
