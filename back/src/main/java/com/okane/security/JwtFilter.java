package com.okane.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        System.out.println("=== JWT FILTER === path: " + request.getRequestURI());
        System.out.println("=== JWT FILTER === header: " + header);

        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("=== JWT FILTER === no token, skipping");
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        System.out.println("=== JWT FILTER === isValid: " + jwtUtil.isValid(token));
        System.out.println("=== JWT FILTER === isAccessToken: " + jwtUtil.isAccessToken(token));
        System.out.println("=== JWT FILTER === email: " + jwtUtil.extractEmail(token));

        if (!jwtUtil.isValid(token)) {
            sendError(response, "Invalid or expired token");
            return;
        }

        if (!jwtUtil.isAccessToken(token)) {
            sendError(response, "Refresh tokens cannot be used for authentication");
            return;
        }

        String email = jwtUtil.extractEmail(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!userDetails.isEnabled()) {
                sendError(response, "Account is disabled");
                return;
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // In your JWT filter, after setting the SecurityContext:
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("=== AUTHORITIES === " + auth.getAuthorities());
        chain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}"
        );
    }
}