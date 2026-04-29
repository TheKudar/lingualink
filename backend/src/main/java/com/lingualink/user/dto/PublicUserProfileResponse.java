package com.lingualink.user.dto;

import com.lingualink.user.entity.UserRole;

import java.time.LocalDateTime;

public record PublicUserProfileResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String avatarUrl,
        UserRole role,
        LocalDateTime createdAt
) {
}
