package com.lingualink.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingualink.auth.dto.AuthResponse;
import com.lingualink.auth.dto.LoginRequest;
import com.lingualink.auth.dto.RegisterRequest;
import com.lingualink.auth.dto.UserMeResponse;
import com.lingualink.auth.security.JwtService;
import com.lingualink.auth.service.AuthService;
import com.lingualink.auth.service.CustomUserDetailsService;
import com.lingualink.common.exception.AppException;
import com.lingualink.user.entity.UserRole;
import com.lingualink.user.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void register_withValidRequest_returns200AndToken() throws Exception {
        RegisterRequest request = new RegisterRequest("new@test.com", "password123");
        AuthResponse response = new AuthResponse("test-jwt-token");
        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("User registered successfully"))
            .andExpect(jsonPath("$.data.accessToken").value("test-jwt-token"));
    }

    @Test
    void register_withMissingEmail_returns400() throws Exception {
        String json = "{\"password\": \"password123\"}";

        mockMvc.perform(post("/api/auth/register")
            .contentType("application/json")
            .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value(containsString("email")));
    }

    @Test
    void register_withInvalidEmailFormat_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("not-an-email", "password123");

        mockMvc.perform(post("/api/auth/register")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_withShortPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("test@test.com", "123");

        mockMvc.perform(post("/api/auth/register")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_withBlankPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("test@test.com", "");

        mockMvc.perform(post("/api/auth/register")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_whenEmailAlreadyExists_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("existing@test.com", "password123");
        when(authService.register(any())).thenThrow(new AppException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    void login_withValidRequest_returns200AndToken() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "password123");
        AuthResponse response = new AuthResponse("login-jwt-token");
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("User logged in successfully"))
            .andExpect(jsonPath("$.data.accessToken").value("login-jwt-token"));
    }

    @Test
    void login_withMissingEmail_returns400() throws Exception {
        String json = "{\"password\": \"password123\"}";

        mockMvc.perform(post("/api/auth/login")
            .contentType("application/json")
            .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_withMissingPassword_returns400() throws Exception {
        String json = "{\"email\": \"user@test.com\"}";

        mockMvc.perform(post("/api/auth/login")
            .contentType("application/json")
            .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_withEmptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
            .contentType("application/json")
            .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void me_withAuthentication_returns200() throws Exception {
        UserMeResponse response = new UserMeResponse(1L, "user@test.com", UserRole.STUDENT, UserStatus.ACTIVE);
        when(authService.me(any())).thenReturn(response);

        mockMvc.perform(get("/api/auth/me")
            .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.email").value("user@test.com"))
            .andExpect(jsonPath("$.data.role").value("STUDENT"));
    }

    @Test
    void me_withoutAuthentication_returns403() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    void me_withInvalidToken_returns403() throws Exception {
        mockMvc.perform(get("/api/auth/me")
            .header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isForbidden());
    }
}
