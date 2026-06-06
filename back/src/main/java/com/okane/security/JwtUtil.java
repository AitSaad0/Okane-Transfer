package com.okane.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key = Keys.hmacShaKeyFor(
            "okane-transfer-secret-key-32-chars!!".getBytes()
    );

    private final long ACCESS_EXPIRATION  = 1000L * 60 * 15;          // 15 min
    private final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7 days

    // ── Token generation ─────────────────────────────────────

    public String generateAccessToken(String email) {
        return build(email, "access", ACCESS_EXPIRATION);
    }

    public String generateRefreshToken(String email) {
        return build(email, "refresh", REFRESH_EXPIRATION);
    }

    private String build(String email, String type, long ttl) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(email)
                .claim("type", type)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttl))
                .signWith(key)
                .compact();
    }

    // ── Token validation ─────────────────────────────────────

    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            return "access".equals(claims(token).get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(claims(token).get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ── Claims extraction ─────────────────────────────────────

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    public String extractEmail(String token) {
        return claims(token).getSubject();
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}