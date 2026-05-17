package com.lingualink.auth.dto;

import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;
import com.lingualink.user.entity.UserRole;
import com.lingualink.user.entity.UserStatus;

public record UserMeResponse(
        Long id,
        String email,
        CourseLanguage nativeLanguage,
        CourseLanguage targetLanguage,
        CourseLevel level,
        UserRole role,
        UserStatus status
) {
}
