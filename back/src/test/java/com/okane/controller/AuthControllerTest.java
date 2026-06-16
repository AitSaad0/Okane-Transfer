package com.okane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okane.dto.requestDto.AuthRequestDTO;
import com.okane.dto.requestDto.RefreshRequestDTO;
import com.okane.dto.requestDto.RegisterRequestDTO;
import com.okane.dto.responseDto.AuthResponseDTO;
import com.okane.dto.responseDto.UserResponseDTO;
import com.okane.entity.enums.Role;
import com.okane.exception.*;
import com.okane.exception.GlobalExceptionHandler;
import com.okane.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock    private AuthServiceImpl authService;
    @InjectMocks private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void register_shouldReturn201WithBothTokens() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("client@okane.com");
        dto.setPassword("password123");
        dto.setNom("Doe");
        dto.setPrenom("John");

        when(authService.register(any())).thenReturn(
                AuthResponseDTO.builder().accessToken("access-token").refreshToken("refresh-token").build());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void register_shouldReturn403WhenRoleIsNotClient() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("admin@okane.com");
        dto.setPassword("password123");
        dto.setNom("Admin");
        dto.setPrenom("User");

        when(authService.register(any()))
                .thenThrow(new UnauthorizedAccessException("Public registration is for clients only"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void register_shouldReturn409WhenEmailAlreadyExists() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("client@okane.com");
        dto.setPassword("password123");
        dto.setNom("Doe");
        dto.setPrenom("John");

        when(authService.register(any()))
                .thenThrow(new UserAlreadyExistsException("Email already in use"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_shouldReturn200WithBothTokens() throws Exception {
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setEmail("client@okane.com");
        dto.setPassword("password123");

        when(authService.login(any())).thenReturn(
                AuthResponseDTO.builder().accessToken("access-token").refreshToken("refresh-token").build());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void login_shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setEmail("client@okane.com");
        dto.setPassword("wrongpassword");

        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturn401WhenAccountIsDisabled() throws Exception {
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setEmail("client@okane.com");
        dto.setPassword("password123");

        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Account is disabled"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_shouldReturn200WithNewTokens() throws Exception {
        RefreshRequestDTO dto = new RefreshRequestDTO();
        dto.setRefreshToken("valid-refresh-token");

        when(authService.refresh(any())).thenReturn(
                AuthResponseDTO.builder().accessToken("new-access-token").refreshToken("new-refresh-token").build());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void refresh_shouldReturn401WhenTokenIsInvalid() throws Exception {
        RefreshRequestDTO dto = new RefreshRequestDTO();
        dto.setRefreshToken("invalid-token");

        when(authService.refresh(any()))
                .thenThrow(new InvalidTokenException("Invalid or expired refresh token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_shouldReturn401WhenAccountIsDisabled() throws Exception {
        RefreshRequestDTO dto = new RefreshRequestDTO();
        dto.setRefreshToken("valid-refresh-token");

        when(authService.refresh(any()))
                .thenThrow(new BadCredentialsException("Account is disabled"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldReturn204() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent());
    }

    @Test
    void me_shouldReturn200WithUserDetails() throws Exception {
        UserResponseDTO response = UserResponseDTO.builder()
                .id(1L)
                .email("client@okane.com")
                .nom("Doe")
                .prenom("John")
                .role(Role.CLIENT)
                .active(true)
                .build();

        when(authService.me("client@okane.com")).thenReturn(response);

        mockMvc.perform(get("/api/auth/me")
                        .accept(MediaType.APPLICATION_JSON)
                        .principal(new UsernamePasswordAuthenticationToken("client@okane.com", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("client@okane.com"))
                .andExpect(jsonPath("$.nom").value("Doe"))
                .andExpect(jsonPath("$.prenom").value("John"))
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void me_shouldReturn401WhenTokenIsInvalid() throws Exception {
        when(authService.me("ghost@okane.com"))
                .thenThrow(new InvalidTokenException("User not found"));

        mockMvc.perform(get("/api/auth/me")
                        .accept(MediaType.APPLICATION_JSON)
                        .principal(new UsernamePasswordAuthenticationToken("ghost@okane.com", null)))
                .andExpect(status().isUnauthorized());
    }
}