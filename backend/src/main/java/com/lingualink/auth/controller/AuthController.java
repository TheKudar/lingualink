package com.lingualink.auth.controller;

import com.lingualink.common.config.OpenApiConfig;
import com.lingualink.auth.dto.AuthResponse;
import com.lingualink.auth.dto.LoginRequest;
import com.lingualink.auth.dto.RegisterRequest;
import com.lingualink.auth.dto.UserMeResponse;
import com.lingualink.auth.service.AuthService;
import com.lingualink.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a user", description = "Creates a new account and returns an access token.")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request), "User registered successfully");
    }

    @PostMapping("/login")
    @Operation(summary = "Log in", description = "Authenticates a user by email and password and returns an access token.")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request), "User logged in successfully");
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current auth user",
            description = "Returns identity details for the authenticated JWT subject.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    public ApiResponse<UserMeResponse> me(@Parameter(hidden = true) Authentication authentication) {
        return ApiResponse.ok(authService.me(authentication));
    }
}
