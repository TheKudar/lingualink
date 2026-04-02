package com.lingualink.user.dto;

import com.lingualink.user.entity.UserRole;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
