package com.lingualink.auth.dto;

import com.lingualink.user.entity.UserRole;
import com.lingualink.user.entity.UserStatus;

public record UserMeResponse(
        Long id,
        String email,
        UserRole role,
        UserStatus status
) {
}
