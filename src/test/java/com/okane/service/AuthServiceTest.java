package com.okane.service;

import com.okane.dto.requestDto.AuthRequestDTO;
import com.okane.dto.requestDto.RefreshRequestDTO;
import com.okane.dto.requestDto.RegisterRequestDTO;
import com.okane.dto.responseDto.AuthResponseDTO;
import com.okane.dto.responseDto.UserResponseDTO;
import com.okane.entity.User;
import com.okane.entity.enums.Role;
import com.okane.exception.*;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository  userRepository;
    @Mock private JwtUtil         jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthServiceImpl authService;

    // ─────────────────────────────────────────────────────────────
    // register()
    // ─────────────────────────────────────────────────────────────

    @Test
    void register_shouldSaveUserAndReturnBothTokens() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("client@okane.com");
        dto.setPassword("password123");
        dto.setNom("Doe");
        dto.setPrenom("John");
        dto.setRole(Role.CLIENT);

        when(userRepository.existsByEmail("client@okane.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(jwtUtil.generateAccessToken("client@okane.com")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("client@okane.com")).thenReturn("refresh-token");

        AuthResponseDTO response = authService.register(dto);

        assertEquals("access-token",  response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_shouldIgnoreRoleAndAlwaysCreateClient() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("admin@okane.com");
        dto.setPassword("password123");
        dto.setNom("Admin");
        dto.setPrenom("User");
        dto.setRole(Role.ADMIN);

        when(userRepository.existsByEmail("admin@okane.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(jwtUtil.generateAccessToken(any())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token");

        AuthResponseDTO response = authService.register(dto);

        assertNotNull(response.getAccessToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("client@okane.com");
        dto.setPassword("password123");
        dto.setNom("Doe");
        dto.setPrenom("John");
        dto.setRole(Role.CLIENT);

        when(userRepository.existsByEmail("client@okane.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(dto));
        verify(userRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // login()
    // ─────────────────────────────────────────────────────────────

    @Test
    void login_shouldReturnBothTokensForValidCredentials() {
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
        when(jwtUtil.generateAccessToken("client@okane.com")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("client@okane.com")).thenReturn("refresh-token");

        AuthResponseDTO response = authService.login(dto);

        assertEquals("access-token",  response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
    }

    @Test
    void login_shouldThrowBadCredentialsWhenUserNotFound() {
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setEmail("unknown@okane.com");
        dto.setPassword("password123");

        when(userRepository.findByEmail("unknown@okane.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
    }

    @Test
    void login_shouldThrowBadCredentialsWhenPasswordIsWrong() {
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

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
    }

    @Test
    void login_shouldThrowBadCredentialsWhenAccountIsDisabled() {
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

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
    }

    // ─────────────────────────────────────────────────────────────
    // refresh()
    // ─────────────────────────────────────────────────────────────

    @Test
    void refresh_shouldReturnNewTokensForValidRefreshToken() {
        RefreshRequestDTO dto = new RefreshRequestDTO();
        dto.setRefreshToken("valid-refresh-token");

        User user = User.builder()
                .email("client@okane.com")
                .password("encodedPassword")
                .role(Role.CLIENT)
                .active(true)
                .build();

        when(jwtUtil.isRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtUtil.extractEmail("valid-refresh-token")).thenReturn("client@okane.com");
        when(userRepository.findByEmail("client@okane.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken("client@okane.com")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("client@okane.com")).thenReturn("refresh-token");

        AuthResponseDTO response = authService.refresh(dto);

        assertEquals("access-token",  response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
    }

    @Test
    void refresh_shouldThrowInvalidTokenWhenTokenIsNotARefreshToken() {
        RefreshRequestDTO dto = new RefreshRequestDTO();
        dto.setRefreshToken("access-token-used-by-mistake");

        when(jwtUtil.isRefreshToken("access-token-used-by-mistake")).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> authService.refresh(dto));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void refresh_shouldThrowInvalidTokenWhenUserNoLongerExists() {
        RefreshRequestDTO dto = new RefreshRequestDTO();
        dto.setRefreshToken("valid-refresh-token");

        when(jwtUtil.isRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtUtil.extractEmail("valid-refresh-token")).thenReturn("deleted@okane.com");
        when(userRepository.findByEmail("deleted@okane.com")).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> authService.refresh(dto));
    }

    @Test
    void refresh_shouldThrowBadCredentialsWhenAccountIsDisabled() {
        RefreshRequestDTO dto = new RefreshRequestDTO();
        dto.setRefreshToken("valid-refresh-token");

        User user = User.builder()
                .email("client@okane.com")
                .password("encodedPassword")
                .role(Role.CLIENT)
                .active(false)
                .build();

        when(jwtUtil.isRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtUtil.extractEmail("valid-refresh-token")).thenReturn("client@okane.com");
        when(userRepository.findByEmail("client@okane.com")).thenReturn(Optional.of(user));

        assertThrows(BadCredentialsException.class, () -> authService.refresh(dto));
    }

    // ─────────────────────────────────────────────────────────────
    // me()
    // ─────────────────────────────────────────────────────────────

    @Test
    void me_shouldReturnUserResponseDTOForValidEmail() {
        User user = User.builder()
                .email("client@okane.com")
                .nom("Doe")
                .prenom("John")
                .role(Role.CLIENT)
                .active(true)
                .build();

        when(userRepository.findByEmail("client@okane.com")).thenReturn(Optional.of(user));

        UserResponseDTO response = authService.me("client@okane.com");

        assertEquals("client@okane.com", response.getEmail());
        assertEquals("Doe",              response.getNom());
        assertEquals("John",             response.getPrenom());
        assertEquals(Role.CLIENT,        response.getRole());
        assertTrue(response.getActive());
    }

    @Test
    void me_shouldThrowInvalidTokenWhenUserNotFound() {
        when(userRepository.findByEmail("ghost@okane.com")).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> authService.me("ghost@okane.com"));
    }
}