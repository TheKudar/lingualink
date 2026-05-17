package com.lingualink.user.dto;

import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Size(min = 2, max = 50)
    private String firstName;

    @Size(min = 2, max = 50)
    private String lastName;

    private String avatarUrl;

    private CourseLanguage nativeLanguage;

    private CourseLanguage targetLanguage;

    private CourseLevel level;
}
