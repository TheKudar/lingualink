package com.lingualink.course.mapper;

import com.lingualink.course.dto.CourseResponse;
import com.lingualink.course.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    @Mapping(target = "creatorName", ignore = true)
    @Mapping(target = "creatorAvatarUrl", ignore = true)
    CourseResponse toResponse(Course course);
}