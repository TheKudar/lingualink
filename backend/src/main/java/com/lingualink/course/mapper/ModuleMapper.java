package com.lingualink.course.mapper;

import com.lingualink.course.dto.ModuleResponse;
import com.lingualink.course.entity.Module;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = LessonMapper.class)
public interface ModuleMapper {
    @Mapping(source = "lessons", target = "lessons")
    ModuleResponse toResponse(Module module);
}
