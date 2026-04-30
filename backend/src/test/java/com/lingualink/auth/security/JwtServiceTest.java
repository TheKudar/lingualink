package com.lingualink.auth.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
            "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3_600_000L);
    }

    @Test
    void generateAccessToken_returnsJwtStructure() {
        String token = jwtService.generateAccessToken("test@example.com");

        assertNotNull(token);
        assertFalse(token.isBlank());
        // JWT has format: header.payload.signature (exactly 2 dots)
        assertEquals(2, token.split("\\.").length);
    }

    @Test
    void extractEmail_roundTrip_returnsOriginalEmail() {
        String email = "user@test.com";
        String token = jwtService.generateAccessToken(email);

        String extracted = jwtService.extractEmail(token);

        assertEquals(email, extracted);
    }

    @Test
    void isTokenValid_withValidToken_returnsTrue() {
        String email = "user@test.com";
        String token = jwtService.generateAccessToken(email);

        assertTrue(jwtService.isTokenValid(token, email));
    }

    @Test
    void isTokenValid_withWrongEmail_returnsFalse() {
        String token = jwtService.generateAccessToken("user@test.com");

        assertFalse(jwtService.isTokenValid(token, "other@test.com"));
    }

    @Test
    void isTokenValid_withExpiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1000L);
        String email = "user@test.com";
        String token = jwtService.generateAccessToken(email);

        // Token is already expired due to negative expiration
        assertFalse(jwtService.isTokenValid(token, email));
    }

    @Test
    void extractEmail_withTamperedToken_throwsJwtException() {
        String token = jwtService.generateAccessToken("test@example.com");
        String tamperedToken = token + "x"; // Append character to break signature

        assertThrows(JwtException.class, () -> jwtService.extractEmail(tamperedToken));
    }
}
