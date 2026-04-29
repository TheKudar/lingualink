package com.lingualink.auth.service;

import com.lingualink.auth.dto.AuthResponse;
import com.lingualink.auth.dto.LoginRequest;
import com.lingualink.auth.dto.RegisterRequest;
import com.lingualink.auth.dto.UserMeResponse;
import com.lingualink.auth.security.JwtService;
import com.lingualink.common.exception.AppException;
import com.lingualink.user.entity.User;
import com.lingualink.user.entity.UserRole;
import com.lingualink.user.entity.UserStatus;
import com.lingualink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        String username = normalizeUsername(request.username());
        UserRole requestedRole = resolveRegistrationRole(request.role());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AppException("User with this email already exists");
        }

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new AppException("User with this username already exists");
        }

        User user = User.builder()
                .email(email)
                .username(username)
                .firstName(normalizeName(request.firstName()))
                .lastName(normalizeName(request.lastName()))
                .password(passwordEncoder.encode(request.password()))
                .role(requestedRole)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        String token = jwtService.generateAccessToken(user.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.password()
                )
        );

        String token = jwtService.generateAccessToken(email);
        return new AuthResponse(token);
    }

    public UserMeResponse me(Authentication authentication) {
        String email = normalizeEmail(authentication.getName());

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AppException("User not found"));

        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeUsername(String username) {
        return username.trim();
    }

    private String normalizeName(String value) {
        return value.trim();
    }

    private UserRole resolveRegistrationRole(UserRole role) {
        if (role == null) {
            return UserRole.STUDENT;
        }
        if (role == UserRole.ADMIN) {
            throw new AppException("Admin registration is not allowed");
        }
        return role;
    }
}
