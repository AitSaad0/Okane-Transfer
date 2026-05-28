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
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtUtil.generateToken("test@okane.com");
        assertNotNull(token);
    }

    @Test
    void extractUsername_shouldReturnCorrectEmail() {
        String token = jwtUtil.generateToken("test@okane.com");
        assertEquals("test@okane.com", jwtUtil.extractUsername(token));
    }

    @Test
    void isValid_shouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken("test@okane.com");
        assertTrue(jwtUtil.isValid(token));
    }

    @Test
    void isValid_shouldReturnFalseForTamperedToken() {
        assertFalse(jwtUtil.isValid("tampered.token.here"));
    }
}