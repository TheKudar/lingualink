package com.lingualink.user.controller;

import com.lingualink.user.dto.ChatUserSearchResponse;
import com.lingualink.user.dto.PublicUserProfileResponse;
import com.lingualink.user.dto.UserDto;
import com.lingualink.user.dto.UserManagementUpdateRequest;
import com.lingualink.user.dto.UserUpdateRequest;
import com.lingualink.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PatchMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(request));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> replaceCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(request));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ChatUserSearchResponse>> searchUsersForChat(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "true") boolean excludeCurrentUser
    ) {
        return ResponseEntity.ok(userService.searchUsersForChat(query, excludeCurrentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicUserProfileResponse> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getPublicProfile(id));
    }

    @PatchMapping("/{id}/management")
    public ResponseEntity<UserDto> updateUserManagement(
            @PathVariable Long id,
            @RequestBody UserManagementUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserManagement(id, request));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<UserDto> blockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.blockUser(id));
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<UserDto> unblockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.unblockUser(id));
    }
}
