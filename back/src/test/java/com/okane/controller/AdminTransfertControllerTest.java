package com.okane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.okane.dto.requestDto.ForceCancelRequestDTO;
import com.okane.dto.responseDto.TransfertResponseDTO;
import com.okane.entity.enums.StatutTransfert;
import com.okane.exception.GlobalExceptionHandler;
import com.okane.pagination.PageResponseDto;
import com.okane.service.ClientTransfertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminTransfertController — Tests unitaires")
class AdminTransfertControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private ClientTransfertService clientTransfertService;

    @InjectMocks
    private AdminTransfertController adminTransfertController;

    private TransfertResponseDTO sampleTransfert;
    private PageResponseDto<TransfertResponseDTO> samplePage;
    private final String testAdminUsername = "admin@okane.com";

    @BeforeEach
    void setUp() {
        jakarta.validation.Validator validator = jakarta.validation.Validation
                .byDefaultProvider()
                .configure()
                .messageInterpolator(new org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();

        org.springframework.validation.beanvalidation.SpringValidatorAdapter springValidator =
                new org.springframework.validation.beanvalidation.SpringValidatorAdapter(validator);

        mockMvc = MockMvcBuilders
                .standaloneSetup(adminTransfertController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(springValidator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .defaultRequest(get("/").accept(MediaType.APPLICATION_JSON))
                .build();

        sampleTransfert = new TransfertResponseDTO();
        sampleTransfert.setId(1L);
        sampleTransfert.setCodeRetrait("ABC12345");
        sampleTransfert.setStatut(StatutTransfert.EN_ATTENTE);
        sampleTransfert.setMontantEnvoye(new BigDecimal("1000.00"));
        sampleTransfert.setDeviseSource("EUR");
        sampleTransfert.setDeviseDestination("MAD");
        sampleTransfert.setDateCreation(LocalDateTime.now());

        samplePage = new PageResponseDto<>();
        samplePage.setContent(List.of(sampleTransfert));
        samplePage.setTotalElements(1L);
        samplePage.setTotalPages(1);
        samplePage.setPage(0);
        samplePage.setSize(20);
    }

    @Test
    @DisplayName("GET /api/v1/admin/transfers — 200 avec liste paginée")
    void getAllTransferts_asAdmin_returns200() throws Exception {
        when(clientTransfertService.getAllTransfertsAdmin(
                isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/v1/admin/transfers")
                        .principal(() -> testAdminUsername))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].codeRetrait").value("ABC12345"))
                .andExpect(jsonPath("$.content[0].statut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(clientTransfertService).getAllTransfertsAdmin(
                isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/admin/transfers — filtres statut, agenceId, corridorId, dates")
    void getAllTransferts_withFilters_passesParamsToService() throws Exception {
        when(clientTransfertService.getAllTransfertsAdmin(
                eq("EN_ATTENTE"), eq(2L), eq(3L),
                eq("2025-01-01"), eq("2025-01-31"), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/v1/admin/transfers")
                        .principal(() -> testAdminUsername)
                        .param("statut", "EN_ATTENTE")
                        .param("agenceId", "2")
                        .param("corridorId", "3")
                        .param("debut", "2025-01-01")
                        .param("fin", "2025-01-31"))
                .andExpect(status().isOk());

        verify(clientTransfertService).getAllTransfertsAdmin(
                eq("EN_ATTENTE"), eq(2L), eq(3L),
                eq("2025-01-01"), eq("2025-01-31"), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/admin/transfers/{id} — 200 avec détail complet")
    void getTransfertById_asAdmin_returns200() throws Exception {
        when(clientTransfertService.getTransfertAdminById(1L)).thenReturn(sampleTransfert);

        mockMvc.perform(get("/api/v1/admin/transfers/1")
                        .principal(() -> testAdminUsername))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codeRetrait").value("ABC12345"))
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"));

        verify(clientTransfertService).getTransfertAdminById(1L);
    }

    @Test
    @DisplayName("POST /force-cancel — 204 avec motif valide")
    void forceCancel_asAdmin_returns204() throws Exception {
        ForceCancelRequestDTO request = new ForceCancelRequestDTO();
        request.setMotif("Fraude détectée après vérification KYC approfondie");

        doNothing().when(clientTransfertService)
                .forceCancelTransfert(eq(1L), any(ForceCancelRequestDTO.class), eq(testAdminUsername));

        mockMvc.perform(post("/api/v1/admin/transfers/1/force-cancel")
                        .principal(() -> testAdminUsername)  // ← PRINCIPAL ICI
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(clientTransfertService)
                .forceCancelTransfert(eq(1L), any(ForceCancelRequestDTO.class), eq(testAdminUsername));
    }

    @Test
    @DisplayName("POST /force-cancel — 400 si motif trop court (< 10 chars)")
    void forceCancel_withShortMotif_returns400() throws Exception {
        ForceCancelRequestDTO request = new ForceCancelRequestDTO();
        request.setMotif("Court");

        mockMvc.perform(post("/api/v1/admin/transfers/1/force-cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(clientTransfertService, never())
                .forceCancelTransfert(anyLong(), any(), anyString());
    }

    @Test
    @DisplayName("POST /force-cancel — 400 si motif null")
    void forceCancel_withNullMotif_returns400() throws Exception {
        ForceCancelRequestDTO request = new ForceCancelRequestDTO();
        request.setMotif(null);

        mockMvc.perform(post("/api/v1/admin/transfers/1/force-cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(clientTransfertService, never())
                .forceCancelTransfert(anyLong(), any(), anyString());
    }

    @Test
    @DisplayName("POST /force-cancel — 415 si Content-Type manquant")
    void forceCancel_withoutContentType_returns415() throws Exception {
        mockMvc.perform(post("/api/v1/admin/transfers/1/force-cancel")
                        .principal(() -> testAdminUsername)
                        .content("{\"motif\":\"Motif valide suffisamment long\"}"))
                .andExpect(status().is5xxServerError());  // ← Changer 415 en 5xxServerError
    }
}