package com.okane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
@DisplayName("ClientTransfertController — Tests unitaires")
class ClientTransfertControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private ClientTransfertService clientTransfertService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ClientTransfertController clientTransfertController;

    private TransfertResponseDTO sampleTransfert;
    private PageResponseDto<TransfertResponseDTO> samplePage;
    private final String testClientUsername = "client@example.com";

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
                .standaloneSetup(clientTransfertController)
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
        samplePage.setSize(10);
    }

    private void setupSecurityContext(String username) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("GET /api/v1/clients/transfers — 200 OK avec liste paginée")
    void getMyTransferts_shouldReturn200() throws Exception {
        setupSecurityContext(testClientUsername);

        when(clientTransfertService.getTransfertsClient(eq(testClientUsername), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/v1/clients/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].codeRetrait").value("ABC12345"))
                .andExpect(jsonPath("$.content[0].statut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(clientTransfertService).getTransfertsClient(eq(testClientUsername), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/clients/transfers — pagination personnalisée")
    void getMyTransferts_withCustomPagination_shouldReturn200() throws Exception {
        setupSecurityContext(testClientUsername);

        when(clientTransfertService.getTransfertsClient(eq(testClientUsername), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/v1/clients/transfers")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/clients/transfers — paramètres négatifs")
    void getMyTransferts_withNegativePagination_shouldUseDefaults() throws Exception {
        setupSecurityContext(testClientUsername);

        when(clientTransfertService.getTransfertsClient(eq(testClientUsername), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/v1/clients/transfers")
                        .param("page", "-1")
                        .param("size", "-5"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/clients/transfers — liste vide")
    void getMyTransferts_whenEmptyList_shouldReturn200() throws Exception {
        setupSecurityContext(testClientUsername);

        PageResponseDto<TransfertResponseDTO> emptyPage = new PageResponseDto<>();
        emptyPage.setContent(List.of());
        emptyPage.setTotalElements(0L);
        emptyPage.setTotalPages(0);

        when(clientTransfertService.getTransfertsClient(eq(testClientUsername), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/clients/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/clients/transfers/{id} — 200 OK")
    void getMyTransfertById_shouldReturn200() throws Exception {
        setupSecurityContext(testClientUsername);

        when(clientTransfertService.getTransfertClientById(1L, testClientUsername))
                .thenReturn(sampleTransfert);

        mockMvc.perform(get("/api/v1/clients/transfers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codeRetrait").value("ABC12345"));
    }

    @Test
    @DisplayName("GET /api/v1/clients/transfers/{id} — appel service avec bon id")
    void getMyTransfertById_passesCorrectIdToService() throws Exception {
        setupSecurityContext(testClientUsername);

        when(clientTransfertService.getTransfertClientById(42L, testClientUsername))
                .thenReturn(sampleTransfert);

        mockMvc.perform(get("/api/v1/clients/transfers/42"))
                .andExpect(status().isOk());

        verify(clientTransfertService).getTransfertClientById(42L, testClientUsername);
    }

    @Test
    @DisplayName("GET /api/v1/clients/transfers/track — 200 OK")
    void trackTransfert_shouldReturn200() throws Exception {
        when(clientTransfertService.trackTransfert("ABC12345"))
                .thenReturn(sampleTransfert);

        mockMvc.perform(get("/api/v1/clients/transfers/track")
                        .param("ref", "ABC12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/clients/transfers/track — ref manquante")
    void trackTransfert_withoutRef_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/clients/transfers/track"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/clients/transfers/track — ref vide")
    void trackTransfert_withEmptyRef_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/clients/transfers/track")
                        .param("ref", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/clients/transfers/track — non trouvé")
    void trackTransfert_whenNotFound_shouldReturn404() throws Exception {
        when(clientTransfertService.trackTransfert("INVALID"))
                .thenThrow(new com.okane.exception.ResourceNotFoundException("Transfert non trouvé"));

        mockMvc.perform(get("/api/v1/clients/transfers/track")
                        .param("ref", "INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/clients/transfers/{id} — statut PAYE")
    void getMyTransfertById_withPayeStatus_shouldReturn200() throws Exception {
        setupSecurityContext(testClientUsername);

        TransfertResponseDTO payeTransfert = new TransfertResponseDTO();
        payeTransfert.setId(2L);
        payeTransfert.setCodeRetrait("XYZ98765");
        payeTransfert.setStatut(StatutTransfert.PAYE);

        when(clientTransfertService.getTransfertClientById(2L, testClientUsername))
                .thenReturn(payeTransfert);

        mockMvc.perform(get("/api/v1/clients/transfers/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("PAYE"));
    }

    @Test
    @DisplayName("GET /api/v1/clients/transfers — tri par dateCreation DESC")
    void getMyTransferts_defaultSorting() throws Exception {
        setupSecurityContext(testClientUsername);

        when(clientTransfertService.getTransfertsClient(eq(testClientUsername), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/v1/clients/transfers"))
                .andExpect(status().isOk());

        verify(clientTransfertService).getTransfertsClient(
                eq(testClientUsername),
                argThat(pageable -> {
                    Sort sort = pageable.getSort();
                    return sort.getOrderFor("dateCreation") != null;
                })
        );
    }
}