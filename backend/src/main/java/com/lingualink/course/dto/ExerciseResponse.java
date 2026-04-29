package com.lingualink.course.dto;

import com.lingualink.course.entity.ExerciseType;

import java.util.List;

public record ExerciseResponse(
        Long id,
        Long lessonId,
        ExerciseType type,
        String question,
        List<String> options,
        String correctAnswer,
        String explanation,
        Integer orderIndex
) {
}
