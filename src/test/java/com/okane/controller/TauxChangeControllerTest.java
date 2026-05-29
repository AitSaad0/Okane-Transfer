package com.okane.controller;

import com.okane.dto.requestDto.TauxChangeRequestDTO;
import com.okane.dto.responseDto.ConversionResponseDTO;
import com.okane.dto.responseDto.TauxChangeResponseDTO;
import com.okane.service.TauxChangeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TauxChangeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TauxChangeService tauxChangeService;

    @InjectMocks
    private TauxChangeController tauxChangeController;

    private ObjectMapper objectMapper;

    private TauxChangeResponseDTO responseDTO;
    private TauxChangeRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(tauxChangeController).build();

        responseDTO = TauxChangeResponseDTO.builder()
                .id(1L)
                .taux(new BigDecimal("655.957"))
                .source("ACTUEL")
                .paysOrigineNom("Sénégal")
                .paysDestinationNom("France")
                .deviseSourceCode("XOF")
                .deviseDestinationCode("EUR")
                .build();

        requestDTO = TauxChangeRequestDTO.builder()
                .tauxNouveau(new BigDecimal("660.000"))
                .source("TEST")
                .build();
    }

    @Test
    void getCurrentRates_shouldReturn200() throws Exception {
        when(tauxChangeService.findAllCurrentRates()).thenReturn(Arrays.asList(responseDTO));

        mockMvc.perform(get("/api/v1/exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paysOrigineNom").value("Sénégal"));
    }

    @Test
    void convert_shouldReturn200() throws Exception {
        ConversionResponseDTO conversion = ConversionResponseDTO.builder()
                .montantSource(new BigDecimal("10000"))
                .deviseSource("XOF")
                .montantConverti(new BigDecimal("15.24"))
                .deviseDestination("EUR")
                .tauxApplique(new BigDecimal("655.957"))
                .message("Taux figé")
                .build();

        when(tauxChangeService.convert("XOF", "EUR", new BigDecimal("10000"))).thenReturn(conversion);

        mockMvc.perform(get("/api/v1/exchange-rates/convert")
                        .param("from", "XOF")
                        .param("to", "EUR")
                        .param("amount", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montantConverti").value(15.24));
    }

    @Test
    void updateManual_shouldReturn200() throws Exception {
        when(tauxChangeService.updateManual(eq(1L), any())).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/admin/exchange-rates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taux").value(655.957));
    }

    @Test
    void syncFromExternalApi_shouldReturn200() throws Exception {
        doNothing().when(tauxChangeService).syncFromExternalApi();

        mockMvc.perform(post("/api/v1/admin/exchange-rates/sync"))
                .andExpect(status().isOk())
                .andExpect(content().string("Synchronisation terminée"));
    }

    @Test
    void getHistory_shouldReturn200() throws Exception {
        when(tauxChangeService.getHistory(1L)).thenReturn(Arrays.asList(responseDTO));

        mockMvc.perform(get("/api/v1/admin/exchange-rates/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].source").value("ACTUEL"));
    }
}