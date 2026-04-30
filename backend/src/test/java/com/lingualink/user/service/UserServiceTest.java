package com.lingualink.user.service;

import com.lingualink.common.exception.AppException;
import com.lingualink.user.dto.UserDto;
import com.lingualink.user.entity.User;
import com.lingualink.user.entity.UserRole;
import com.lingualink.user.entity.UserStatus;
import com.lingualink.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(String principal) {
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getCurrentUser_documentsBugCallsFindByUsernameNotEmail() {
        // BUG #1 CHARACTERIZATION TEST
        // The JWT subject and Security context principal is the EMAIL,
        // but getCurrentUser() calls findByUsername(username) instead of findByEmail(email).
        // This test documents the current (buggy) behavior: it uses findByUsername.
        // This test should be DELETED once the bug is fixed.

        String email = "user@test.com";
        setSecurityContext(email);

        User user = User.builder()
            .id(1L)
            .email(email)
            .username("john_doe")
            .firstName("John")
            .lastName("Doe")
            .role(UserRole.STUDENT)
            .status(UserStatus.ACTIVE)
            .build();

        // Mock findByUsername (the buggy call)
        when(userRepository.findByUsername(email)).thenReturn(Optional.of(user));

        UserDto result = userService.getCurrentUser();

        assertEquals(email, result.email());
        assertEquals("John", result.firstName());
        verify(userRepository).findByUsername(email);
    }

    @Test
    @Disabled("BUG #1: getCurrentUser() calls findByUsername(email) but should call findByEmail(email). " +
        "Security context stores email as principal, so findByUsername(email) will fail at runtime.")
    void getCurrentUser_shouldCallFindByEmail_failsUntilFixed() {
        // Once the bug is fixed, this test should PASS and replace the characterization test above.
        String email = "user@test.com";
        setSecurityContext(email);

        User user = User.builder()
            .id(1L)
            .email(email)
            .username("john_doe")
            .firstName("John")
            .lastName("Doe")
            .role(UserRole.STUDENT)
            .status(UserStatus.ACTIVE)
            .build();

        // This is the correct mock: findByEmail
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDto result = userService.getCurrentUser();

        assertEquals(email, result.email());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getCurrentUser_whenUserNotFound_throwsAppException() {
        setSecurityContext("missing@test.com");
        when(userRepository.findByUsername("missing@test.com")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> userService.getCurrentUser());
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @Disabled("BUG #2: UserService has @Transactional(readOnly=true) at class level. " +
        "The update() method calls userRepository.save(user), but this happens within a read-only transaction. " +
        "Hibernate will silently ignore the flush or throw TransactionRequiredException at runtime. " +
        "update() needs its own @Transactional (non-readOnly) override to work correctly.")
    void update_writePath_saveShouldBeCalled() {
        // This test documents the intent: update() SHOULD call save().
        // But it will fail in a real Spring context due to the read-only transaction.
        // The fix: add @Transactional (writable) to update().

        String email = "user@test.com";
        setSecurityContext(email);

        User existingUser = User.builder()
            .id(1L)
            .email(email)
            .username("john_doe")
            .firstName("John")
            .lastName("Doe")
            .role(UserRole.STUDENT)
            .status(UserStatus.ACTIVE)
            .build();

        when(userRepository.findByUsername(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserDto updateDto = UserDto.builder()
            .firstName("Jane")
            .build();

        userService.update(updateDto);

        // In a real Spring context, this verify would fail because save() in a read-only
        // transaction is silently ignored or throws. But in a pure Mockito test,
        // the @Transactional annotation is invisible, so the call is made.
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_updatesOnlyNonNullFields() {
        String email = "user@test.com";
        setSecurityContext(email);

        User existingUser = User.builder()
            .id(1L)
            .email(email)
            .username("john_doe")
            .firstName("John")
            .lastName("Doe")
            .avatarUrl("https://example.com/old.jpg")
            .role(UserRole.STUDENT)
            .status(UserStatus.ACTIVE)
            .build();

        when(userRepository.findByUsername(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto updateDto = UserDto.builder()
            .firstName("Jane")
            .lastName(null)
            .avatarUrl(null)
            .build();

        userService.update(updateDto);

        verify(userRepository).save(argThat(user ->
            "Jane".equals(user.getFirstName()) &&
            "Doe".equals(user.getLastName()) &&
            "https://example.com/old.jpg".equals(user.getAvatarUrl())
        ));
    }

    @Test
    void update_updatesAllProvidedFields() {
        String email = "user@test.com";
        setSecurityContext(email);

        User existingUser = User.builder()
            .id(1L)
            .email(email)
            .username("john_doe")
            .firstName("John")
            .lastName("Doe")
            .avatarUrl("https://example.com/old.jpg")
            .role(UserRole.STUDENT)
            .status(UserStatus.ACTIVE)
            .build();

        when(userRepository.findByUsername(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto updateDto = UserDto.builder()
            .firstName("Jane")
            .lastName("Smith")
            .avatarUrl("https://example.com/new.jpg")
            .build();

        userService.update(updateDto);

        verify(userRepository).save(argThat(user ->
            "Jane".equals(user.getFirstName()) &&
            "Smith".equals(user.getLastName()) &&
            "https://example.com/new.jpg".equals(user.getAvatarUrl())
        ));
    }
}
