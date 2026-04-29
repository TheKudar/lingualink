package com.lingualink.course.mapper;

import com.lingualink.course.dto.LessonResponse;
import com.lingualink.course.entity.Lesson;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    LessonResponse toResponse(Lesson lesson);
}