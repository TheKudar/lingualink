package com.lingualink.auth.service;

import com.lingualink.common.exception.AppException;
import com.lingualink.auth.dto.AuthResponse;
import com.lingualink.auth.dto.LoginRequest;
import com.lingualink.auth.dto.RegisterRequest;
import com.lingualink.auth.dto.UserMeResponse;
import com.lingualink.auth.security.JwtService;
import com.lingualink.user.entity.User;
import com.lingualink.user.entity.UserRole;
import com.lingualink.user.entity.UserStatus;
import com.lingualink.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(jwtService.generateAccessToken(anyString())).thenReturn("test-token");
    }

    @Test
    void register_withNewEmail_savesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("new@test.com", "password123");
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        AuthResponse response = authService.register(request);

        assertEquals("test-token", response.accessToken());
        verify(userRepository).existsByEmail("new@test.com");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_withDuplicateEmail_throwsAppException() {
        RegisterRequest request = new RegisterRequest("existing@test.com", "password123");
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> authService.register(request));
        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_encodesPassword() {
        RegisterRequest request = new RegisterRequest("test@test.com", "rawPassword");
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(argThat(user -> "encodedPassword".equals(user.getPassword())));
    }

    @Test
    void register_setsRoleAndStatus() {
        RegisterRequest request = new RegisterRequest("test@test.com", "password");
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        verify(userRepository).save(argThat(user ->
            user.getRole() == UserRole.STUDENT && user.getStatus() == UserStatus.ACTIVE
        ));
    }

    @Test
    void login_withValidCredentials_callsAuthManagerAndReturnsToken() {
        LoginRequest request = new LoginRequest("user@test.com", "password");
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        AuthResponse response = authService.login(request);

        assertEquals("test-token", response.accessToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateAccessToken("user@test.com");
    }

    @Test
    void login_callsAuthManagerWithCorrectCredentials() {
        LoginRequest request = new LoginRequest("test@test.com", "mypassword");
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));

        authService.login(request);

        verify(authenticationManager).authenticate(argThat(token ->
            token instanceof UsernamePasswordAuthenticationToken &&
            "test@test.com".equals(token.getPrincipal()) &&
            "mypassword".equals(token.getCredentials().toString())
        ));
    }

    @Test
    void me_withValidAuthentication_returnsUserMeResponse() {
        User user = User.builder()
            .id(1L)
            .email("user@test.com")
            .role(UserRole.STUDENT)
            .status(UserStatus.ACTIVE)
            .build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@test.com");

        UserMeResponse response = authService.me(auth);

        assertEquals(1L, response.id());
        assertEquals("user@test.com", response.email());
        assertEquals(UserRole.STUDENT, response.role());
        assertEquals(UserStatus.ACTIVE, response.status());
    }

    @Test
    void me_whenUserNotFound_throwsAppException() {
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("nonexistent@test.com");

        AppException exception = assertThrows(AppException.class, () -> authService.me(auth));
        assertTrue(exception.getMessage().contains("not found"));
    }
}
