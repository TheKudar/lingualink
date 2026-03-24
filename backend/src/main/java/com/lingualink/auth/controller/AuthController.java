package com.lingualink.auth.controller;

import com.lingualink.auth.dto.AuthResponse;
import com.lingualink.auth.dto.LoginRequest;
import com.lingualink.auth.dto.RegisterRequest;
import com.lingualink.auth.dto.UserMeResponse;
import com.lingualink.auth.service.AuthService;
import com.lingualink.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request), "User registered successfully");
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request), "User logged in successfully");
    }

    @GetMapping("/me")
    public ApiResponse<UserMeResponse> me(Authentication authentication) {
        return ApiResponse.ok(authService.me(authentication));
    }
}
