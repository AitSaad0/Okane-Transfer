package com.okane.controller;

import com.okane.dto.requestDto.OtpVerifyRequestDTO;
import com.okane.entity.User;
import com.okane.service.OtpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OtpControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private OtpController otpController;

    private ObjectMapper objectMapper;
    private User mockUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        mockUser = new User();
        mockUser.setId(1L);

        // set directly in SecurityContextHolder — this is what AuthenticationPrincipalArgumentResolver reads
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(mockUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc = MockMvcBuilders
                .standaloneSetup(otpController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void send_shouldReturn200() throws Exception {
        doNothing().when(otpService).send(1L);

        mockMvc.perform(post("/api/auth/2fa/send"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP sent successfully"));

        verify(otpService, times(1)).send(1L);
    }

    @Test
    void send_shouldCallServiceWithCorrectUserId() throws Exception {
        doNothing().when(otpService).send(1L);

        mockMvc.perform(post("/api/auth/2fa/send"))
                .andExpect(status().isOk());

        verify(otpService).send(1L);
    }

    @Test
    void verify_shouldReturn200() throws Exception {
        OtpVerifyRequestDTO request = new OtpVerifyRequestDTO();
        request.setValeur("123456");

        doNothing().when(otpService).verify(1L, "123456");

        mockMvc.perform(post("/api/auth/2fa/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP verified successfully"));

        verify(otpService, times(1)).verify(1L, "123456");
    }

    @Test
    void verify_shouldCallServiceWithCorrectUserIdAndValeur() throws Exception {
        OtpVerifyRequestDTO request = new OtpVerifyRequestDTO();
        request.setValeur("654321");

        doNothing().when(otpService).verify(1L, "654321");

        mockMvc.perform(post("/api/auth/2fa/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(otpService).verify(1L, "654321");
    }
}