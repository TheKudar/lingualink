package com.lingualink.user.dto;

import com.lingualink.user.entity.UserRole;
import com.lingualink.user.entity.UserStatus;
import lombok.Data;

@Data
public class UserManagementUpdateRequest {
    private UserRole role;
    private UserStatus status;
}
