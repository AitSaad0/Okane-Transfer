package com.okane.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key = Keys.hmacShaKeyFor(
            "okane-transfer-secret-key-32-chars!!".getBytes(StandardCharsets.UTF_8)
    );

    private final long ACCESS_EXPIRATION  = 1000L * 60 * 15;
    private final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 7;

    // ── Token generation ─────────────────────────────────────

    public String generateAccessToken(String email, String role) {
        return build(email, "access", ACCESS_EXPIRATION, role);
    }

    public String generateAccessToken(String email) {
        return build(email, "access", ACCESS_EXPIRATION, null);
    }

    public String generateRefreshToken(String email) {
        return build(email, "refresh", REFRESH_EXPIRATION, null);
    }

    private String build(String email, String type, long ttl, String role) {
        long now = System.currentTimeMillis();
        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .claim("type", type)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttl))
                .signWith(key);
        if (role != null) {
            String cleanRole = role.replace("ROLE_", "");
            builder.claim("role", cleanRole);
        }
        return builder.compact();
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

    public String extractRole(String token) {
        return claims(token).get("role", String.class);
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
