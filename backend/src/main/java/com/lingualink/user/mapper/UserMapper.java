package com.lingualink.user.mapper;


import com.lingualink.user.dto.UserDto;
import com.lingualink.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserDto toDto(User user);
}