package com.lingualink.user.service;

import com.lingualink.common.exception.AppException;
import com.lingualink.common.storage.LocalFileStorageService;
import com.lingualink.user.dto.ChatUserSearchResponse;
import com.lingualink.user.dto.PublicUserProfileResponse;
import com.lingualink.user.dto.UserDto;
import com.lingualink.user.dto.UserManagementUpdateRequest;
import com.lingualink.user.dto.UserUpdateRequest;
import com.lingualink.user.entity.User;
import com.lingualink.user.entity.UserRole;
import com.lingualink.user.entity.UserStatus;
import com.lingualink.user.mapper.UserMapper;
import com.lingualink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final LocalFileStorageService localFileStorageService;

    public UserDto getCurrentUser() {
        return userMapper.toDto(getAuthenticatedUser());
    }

    @Transactional
    public UserDto update(UserUpdateRequest request) {
        User user = getAuthenticatedUser();

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }

        User updated = userRepository.save(user);
        return userMapper.toDto(updated);
    }

    public PublicUserProfileResponse getPublicProfile(Long id) {
        return userMapper.toPublicProfile(getUserById(id));
    }

    public List<ChatUserSearchResponse> searchUsersForChat(String query, boolean excludeCurrentUser) {
        Long excludeUserId = excludeCurrentUser ? getAuthenticatedUser().getId() : null;

        return userRepository.searchActiveUsers(normalizeSearchQuery(query), UserStatus.ACTIVE, excludeUserId)
                .stream()
                .map(this::toChatUserSearchResponse)
                .toList();
    }

    @Transactional
    public UserDto uploadAvatar(MultipartFile file) {
        User user = getAuthenticatedUser();
        String avatarUrl = localFileStorageService.saveImage(file, "avatars");
        user.setAvatarUrl(avatarUrl);
        return userMapper.toDto(userRepository.save(user));
    }

    public List<ChatUserSearchResponse> searchUsersForChat(String query, boolean excludeCurrentUser) {
        Long excludeUserId = excludeCurrentUser ? getAuthenticatedUser().getId() : null;

        return userRepository.searchActiveUsers(normalizeSearchQuery(query), UserStatus.ACTIVE, excludeUserId)
                .stream()
                .map(this::toChatUserSearchResponse)
                .toList();
    }

    @Transactional
    public UserDto updateUserManagement(Long id, UserManagementUpdateRequest request) {
        requireAdmin();

        User user = getUserById(id);
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        User updated = userRepository.save(user);
        return userMapper.toDto(updated);
    }

    @Transactional
    public UserDto blockUser(Long id) {
        requireAdmin();
        User user = getUserById(id);
        user.setStatus(UserStatus.BLOCKED);
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto unblockUser(Long id) {
        requireAdmin();
        User user = getUserById(id);
        user.setStatus(UserStatus.ACTIVE);
        return userMapper.toDto(userRepository.save(user));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AppException("User not found with email: " + email));
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return getUserByEmail(email);
    }

    private String normalizeSearchQuery(String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ChatUserSearchResponse toChatUserSearchResponse(User user) {
        return new ChatUserSearchResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                user.getRole()
        );
    }

    private void requireAdmin() {
        User currentUser = getAuthenticatedUser();
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can perform this action");
        }
    }
}
