package com.lingualink.user.mapper;


import com.lingualink.user.dto.PublicUserProfileResponse;
import com.lingualink.user.dto.UserDto;
import com.lingualink.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    PublicUserProfileResponse toPublicProfile(User user);
}
