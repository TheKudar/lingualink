package com.lingualink.user.service;

import com.lingualink.user.dto.UserDto;
import com.lingualink.user.entity.User;
import com.lingualink.user.mapper.UserMapper;
import com.lingualink.user.repository.UserRepository;
import com.lingualink.common.exception.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private UserRepository userRepository;

    @Autowired
    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException("User not found"));
        return UserMapper.INSTANCE.toDto(user);
    }
    
    @Transactional
    public UserDto update(UserDto userDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found"));

        if (userDto.getFirstName() != null) {
            user.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            user.setLastName(userDto.getLastName());
        }
        if (userDto.getAvatarUrl() != null) {
            user.setAvatarUrl(userDto.getAvatarUrl());
        }

        User updated = userRepository.save(user);
        return UserMapper.INSTANCE.toDto(updated);
    }
}
