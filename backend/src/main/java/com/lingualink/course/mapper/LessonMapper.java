package com.lingualink.course.mapper;

import com.lingualink.course.dto.LessonResponse;
import com.lingualink.course.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    @Mapping(target = "completed", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    LessonResponse toResponse(Lesson lesson);
}
