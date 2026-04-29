package com.lingualink.user.controller;

import com.lingualink.common.config.OpenApiConfig;
import com.lingualink.user.dto.ChatUserSearchResponse;
import com.lingualink.user.dto.PublicUserProfileResponse;
import com.lingualink.user.dto.UserDto;
import com.lingualink.user.dto.UserManagementUpdateRequest;
import com.lingualink.user.dto.UserUpdateRequest;
import com.lingualink.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Returns the authenticated user's profile.")
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PatchMapping("/me")
    @Operation(summary = "Update my profile", description = "Partially updates the authenticated user's editable profile fields.")
    public ResponseEntity<UserDto> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(request));
    }

    @PutMapping("/me")
    @Operation(summary = "Replace my profile fields", description = "Updates the authenticated user's editable profile fields.")
    public ResponseEntity<UserDto> replaceCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(request));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload my avatar", description = "Uploads or replaces the authenticated user's avatar image.")
    public ResponseEntity<UserDto> uploadAvatar(@Parameter(description = "Avatar image file") @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.uploadAvatar(file));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users for chat", description = "Finds users that can be selected as chat participants.")
    public ResponseEntity<List<ChatUserSearchResponse>> searchUsersForChat(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "true") boolean excludeCurrentUser
    ) {
        return ResponseEntity.ok(userService.searchUsersForChat(query, excludeCurrentUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get public user profile", description = "Returns public profile details for a user.")
    public ResponseEntity<PublicUserProfileResponse> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getPublicProfile(id));
    }

    @PatchMapping("/{id}/management")
    @Operation(summary = "Update user management fields", description = "Admin endpoint for changing a user's role or status.")
    public ResponseEntity<UserDto> updateUserManagement(
            @PathVariable Long id,
            @RequestBody UserManagementUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserManagement(id, request));
    }

    @PostMapping("/{id}/block")
    @Operation(summary = "Block a user", description = "Admin endpoint that marks a user as blocked.")
    public ResponseEntity<UserDto> blockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.blockUser(id));
    }

    @PostMapping("/{id}/unblock")
    @Operation(summary = "Unblock a user", description = "Admin endpoint that restores a blocked user.")
    public ResponseEntity<UserDto> unblockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.unblockUser(id));
    }
}
