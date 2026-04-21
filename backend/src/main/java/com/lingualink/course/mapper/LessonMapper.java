package com.lingualink.course.mapper;

import com.lingualink.course.dto.LessonResponse;
import com.lingualink.course.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LessonMapper {
    LessonMapper INSTANCE = Mappers.getMapper(LessonMapper.class);

    LessonResponse toResponse(Lesson lesson);
}