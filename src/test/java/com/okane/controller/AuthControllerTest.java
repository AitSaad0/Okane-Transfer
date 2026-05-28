package com.okane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.okane.dto.requestDto.AuthRequestDTO;
import com.okane.dto.requestDto.RegisterRequestDTO;
import com.okane.dto.responseDto.AuthResponseDTO;
import com.okane.entity.enums.Role;
import com.okane.exception.GlobalExceptionHandler;
import com.okane.exception.ResourceNotFoundException;
import com.okane.exception.UnauthorizedAccessException;
import com.okane.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock  private AuthService authService;
    @InjectMocks private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_shouldReturn200WithToken() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("client@okane.com");
        dto.setPassword("password123");
        dto.setNom("Doe");
        dto.setPrenom("John");
        dto.setRole(Role.CLIENT);

        when(authService.register(any())).thenReturn(new AuthResponseDTO("mock-token"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"));
    }

    @Test
    void register_shouldReturn403WhenRoleIsNotClient() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("admin@okane.com");
        dto.setPassword("password123");
        dto.setNom("Admin");
        dto.setPrenom("User");
        dto.setRole(Role.ADMIN);

        when(authService.register(any()))
                .thenThrow(new UnauthorizedAccessException("Public registration is for clients only"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_shouldReturn200WithToken() throws Exception {
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setEmail("client@okane.com");
        dto.setPassword("password123");

        when(authService.login(any())).thenReturn(new AuthResponseDTO("mock-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"));
    }

    @Test
    void login_shouldReturn404WhenUserNotFound() throws Exception {
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setEmail("unknown@okane.com");
        dto.setPassword("password123");

        when(authService.login(any()))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }
}