package com.lingualink.auth.service;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_activeUser_returnsEnabledUserDetails() {
        User user = User.builder()
            .id(1L)
            .email("active@test.com")
            .password("encoded-password")
            .role(UserRole.STUDENT)
            .status(UserStatus.ACTIVE)
            .build();
        when(userRepository.findByEmail("active@test.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("active@test.com");

        assertEquals("active@test.com", userDetails.getUsername());
        assertEquals("encoded-password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsername_blockedUser_returnsDisabledUserDetails() {
        User user = User.builder()
            .id(1L)
            .email("blocked@test.com")
            .password("encoded-password")
            .role(UserRole.STUDENT)
            .status(UserStatus.BLOCKED)
            .build();
        when(userRepository.findByEmail("blocked@test.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("blocked@test.com");

        assertFalse(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsername_userNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername("nonexistent@test.com"));
    }

    @Test
    void loadUserByUsername_studentRole_hasRoleStudentAuthority() {
        User user = User.builder()
            .id(1L)
            .email("student@test.com")
            .password("password")
            .role(UserRole.STUDENT)
            .status(UserStatus.ACTIVE)
            .build();
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("student@test.com");

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertTrue(authorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT")));
    }

    @Test
    void loadUserByUsername_creatorRole_hasRoleCreatorAuthority() {
        User user = User.builder()
            .id(1L)
            .email("creator@test.com")
            .password("password")
            .role(UserRole.CREATOR)
            .status(UserStatus.ACTIVE)
            .build();
        when(userRepository.findByEmail("creator@test.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("creator@test.com");

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertTrue(authorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_CREATOR")));
    }

    @Test
    void loadUserByUsername_usesEmail_callsFindByEmailNotUsername() {
        User user = User.builder()
            .id(1L)
            .email("test@example.com")
            .password("password")
            .role(UserRole.STUDENT)
            .status(UserStatus.ACTIVE)
            .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        customUserDetailsService.loadUserByUsername("test@example.com");

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_returnsAllRequiredAuthorities() {
        User user = User.builder()
            .id(1L)
            .email("user@test.com")
            .password("password")
            .role(UserRole.STUDENT)
            .status(UserStatus.ACTIVE)
            .build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user@test.com");

        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
    }
}
