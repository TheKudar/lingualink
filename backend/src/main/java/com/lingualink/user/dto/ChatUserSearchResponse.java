package com.lingualink.user.dto;

import com.lingualink.user.entity.UserRole;

public record ChatUserSearchResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String avatarUrl,
        UserRole role
) {
}
