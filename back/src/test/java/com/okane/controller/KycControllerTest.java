package com.okane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okane.dto.requestDto.IdentityVerificationRequest;
import com.okane.dto.requestDto.WatchlistCheckRequest;
import com.okane.dto.responseDto.IdentityVerificationResponse;
import com.okane.dto.responseDto.WatchlistCheckResponse;
import com.okane.service.KycService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class KycControllerTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    @Mock
    private KycService kycService;
    @InjectMocks
    private KycController kycController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(kycController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void verifyIdentity_ShouldReturnOk() throws Exception {
        IdentityVerificationRequest request = new IdentityVerificationRequest();
        IdentityVerificationResponse response = IdentityVerificationResponse.builder().build();
        when(kycService.verifyIdentity(any(IdentityVerificationRequest.class))).thenReturn(response);
        mockMvc.perform(post("/api/v1/kyc/verify-identity")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void checkWatchlist_ShouldReturnOk() throws Exception {
        WatchlistCheckRequest request = new WatchlistCheckRequest();
        WatchlistCheckResponse response = WatchlistCheckResponse.builder().build();
        when(kycService.checkWatchlist(any(WatchlistCheckRequest.class))).thenReturn(response);
        mockMvc.perform(post("/api/v1/kyc/check-watchlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
