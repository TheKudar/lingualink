package com.lingualink.course.mapper;

import com.lingualink.course.dto.ModuleResponse;
import com.lingualink.course.entity.Module;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = LessonMapper.class)
public interface ModuleMapper {
    ModuleMapper INSTANCE = Mappers.getMapper(ModuleMapper.class);

    @Mapping(source = "lessons", target = "lessons")
    ModuleResponse toResponse(Module module);
}