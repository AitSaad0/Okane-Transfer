package com.okane.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void generateAccessToken_shouldReturnNonNullToken() {
        String token = jwtUtil.generateAccessToken("test@okane.com");
        assertNotNull(token);
    }

    @Test
    void generateRefreshToken_shouldReturnNonNullToken() {
        String token = jwtUtil.generateRefreshToken("test@okane.com");
        assertNotNull(token);
    }

    @Test
    void extractUsername_shouldReturnCorrectEmail() {
        String token = jwtUtil.generateAccessToken("test@okane.com");
        assertEquals("test@okane.com", jwtUtil.extractUsername(token));
    }

    @Test
    void isValid_shouldReturnTrueForValidToken() {
        String token = jwtUtil.generateAccessToken("test@okane.com");
        assertTrue(jwtUtil.isValid(token));
    }

    @Test
    void isValid_shouldReturnFalseForTamperedToken() {
        assertFalse(jwtUtil.isValid("tampered.token.here"));
    }

    @Test
    void isAccessToken_shouldReturnTrueForAccessToken() {
        String token = jwtUtil.generateAccessToken("test@okane.com");
        assertTrue(jwtUtil.isAccessToken(token));
        assertFalse(jwtUtil.isRefreshToken(token));
    }

    @Test
    void isRefreshToken_shouldReturnTrueForRefreshToken() {
        String token = jwtUtil.generateRefreshToken("test@okane.com");
        assertTrue(jwtUtil.isRefreshToken(token));
        assertFalse(jwtUtil.isAccessToken(token));
    }
}