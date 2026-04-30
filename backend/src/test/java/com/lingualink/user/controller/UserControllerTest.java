package com.lingualink.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingualink.auth.security.JwtService;
import com.lingualink.auth.service.CustomUserDetailsService;
import com.lingualink.common.exception.AppException;
import com.lingualink.user.dto.UserDto;
import com.lingualink.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "user@test.com")
    void getCurrentUser_withAuth_returns200() throws Exception {
        UserDto userDto = UserDto.builder()
            .id(1L)
            .username("john_doe")
            .email("user@test.com")
            .firstName("John")
            .lastName("Doe")
            .role("STUDENT")
            .build();
        when(userService.getCurrentUser()).thenReturn(userDto);

        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("user@test.com"))
            .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getCurrentUser_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void getCurrentUser_whenServiceThrowsException_returns400() throws Exception {
        when(userService.getCurrentUser()).thenThrow(new AppException("User not found"));

        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void updateCurrentUser_withAuth_returns200() throws Exception {
        UserDto updateDto = UserDto.builder()
            .id(1L)
            .username("john_doe")
            .email("user@test.com")
            .firstName("Jane")
            .lastName("Smith")
            .role("STUDENT")
            .build();

        when(userService.update(any())).thenReturn(updateDto);

        mockMvc.perform(put("/api/users/me")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Jane"))
            .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void updateCurrentUser_noValidationEnforced_acceptsShortFirstName() throws Exception {
        // DOCUMENTS BUG: UserController.updateCurrentUser() does NOT use @Valid,
        // and accepts UserDto (which has no @Size constraints) rather than UserUpdateRequest.
        // This test shows that a 1-character firstName bypasses validation.

        UserDto updateDto = UserDto.builder()
            .firstName("A")
            .lastName("Smith")
            .build();

        UserDto savedDto = UserDto.builder()
            .id(1L)
            .username("john_doe")
            .email("user@test.com")
            .firstName("A")
            .lastName("Smith")
            .role("STUDENT")
            .build();

        when(userService.update(any())).thenReturn(savedDto);

        mockMvc.perform(put("/api/users/me")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("A"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void updateCurrentUser_withAuth_callsUpdateService() throws Exception {
        UserDto updateDto = UserDto.builder()
            .firstName("Updated")
            .lastName("Name")
            .build();

        UserDto result = UserDto.builder()
            .id(1L)
            .firstName("Updated")
            .lastName("Name")
            .email("user@test.com")
            .role("STUDENT")
            .build();

        when(userService.update(any())).thenReturn(result);

        mockMvc.perform(put("/api/users/me")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk());
    }

    @Test
    void updateCurrentUser_withoutAuth_returns403() throws Exception {
        UserDto updateDto = UserDto.builder()
            .firstName("Jane")
            .build();

        mockMvc.perform(put("/api/users/me")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void updateCurrentUser_whenServiceThrowsException_returns400() throws Exception {
        UserDto updateDto = UserDto.builder()
            .firstName("Jane")
            .build();

        when(userService.update(any())).thenThrow(new AppException("User not found"));

        mockMvc.perform(put("/api/users/me")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
}
