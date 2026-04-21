package com.lingualink.course.mapper;

import com.lingualink.course.dto.CourseResponse;
import com.lingualink.course.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CourseMapper {
    CourseMapper INSTANCE = Mappers.getMapper(CourseMapper.class);

    @Mapping(target = "creatorName", ignore = true)
    @Mapping(target = "creatorAvatarUrl", ignore = true)
    CourseResponse toResponse(Course course);
}