package com.lingualink.user.dto;

import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;
import com.lingualink.user.entity.UserRole;
import com.lingualink.user.entity.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private CourseLanguage nativeLanguage;
    private CourseLanguage targetLanguage;
    private CourseLevel level;
    private UserRole role;
    private UserStatus status;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
