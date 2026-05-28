package com.okane.service;

import com.okane.dto.requestDto.AuthRequestDTO;
import com.okane.dto.responseDto.AuthResponseDTO;
import com.okane.dto.requestDto.RegisterRequestDTO;
import com.okane.entity.User;
import com.okane.entity.enums.Role;
import com.okane.exception.ResourceNotFoundException;
import com.okane.exception.UnauthorizedAccessException;
import com.okane.repository.UserRepository;
import com.okane.security.JwtUtil;
import com.okane.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthServiceImpl authService;

    @Test
    void register_shouldSaveUserAndReturnToken() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("client@okane.com");
        dto.setPassword("password123");
        dto.setNom("Doe");
        dto.setPrenom("John");
        dto.setRole(Role.CLIENT);

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(jwtUtil.generateToken("client@okane.com")).thenReturn("mock-token");

        AuthResponseDTO response = authService.register(dto);

        assertEquals("mock-token", response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_shouldThrowWhenRoleIsNotClient() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("admin@okane.com");
        dto.setPassword("password123");
        dto.setNom("Admin");
        dto.setPrenom("User");
        dto.setRole(Role.ADMIN);

        assertThrows(UnauthorizedAccessException.class, () -> authService.register(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnTokenForValidCredentials() {
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setEmail("client@okane.com");
        dto.setPassword("password123");

        User user = User.builder()
                .email("client@okane.com")
                .password("encodedPassword")
                .role(Role.CLIENT)
                .active(true)
                .build();

        when(userRepository.findByEmail("client@okane.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("client@okane.com")).thenReturn("mock-token");

        AuthResponseDTO response = authService.login(dto);
        assertEquals("mock-token", response.getToken());
    }

    @Test
    void login_shouldThrowWhenUserNotFound() {
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setEmail("unknown@okane.com");
        dto.setPassword("password123");

        when(userRepository.findByEmail("unknown@okane.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(dto));
    }

    @Test
    void login_shouldThrowWhenPasswordIsWrong() {
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setEmail("client@okane.com");
        dto.setPassword("wrongpassword");

        User user = User.builder()
                .email("client@okane.com")
                .password("encodedPassword")
                .role(Role.CLIENT)
                .active(true)
                .build();

        when(userRepository.findByEmail("client@okane.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> authService.login(dto));
    }

    @Test
    void login_shouldThrowWhenAccountIsDisabled() {
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setEmail("client@okane.com");
        dto.setPassword("password123");

        User user = User.builder()
                .email("client@okane.com")
                .password("encodedPassword")
                .role(Role.CLIENT)
                .active(false)
                .build();

        when(userRepository.findByEmail("client@okane.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> authService.login(dto));
    }
}