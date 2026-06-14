package com.okane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.okane.dto.requestDto.ClotureCaisseRequestDTO;
import com.okane.dto.requestDto.EcartCaisseRequestDTO;
import com.okane.dto.responseDto.CaisseResponseDTO;
import com.okane.exception.GlobalExceptionHandler;
import com.okane.service.CaisseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CaisseController — Tests unitaires")
class CaisseControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private CaisseService caisseService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CaisseController caisseController;

    private CaisseResponseDTO sampleCaisse;
    private final String testUsername = "karim.alami@okane.ma";
    private final String managerUsername = "manager@okane.ma";

    @BeforeEach
    void setUp() {
        // Setup validation
        jakarta.validation.Validator validator = jakarta.validation.Validation
                .byDefaultProvider()
                .configure()
                .messageInterpolator(new org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();

        org.springframework.validation.beanvalidation.SpringValidatorAdapter springValidator =
                new org.springframework.validation.beanvalidation.SpringValidatorAdapter(validator);

        mockMvc = MockMvcBuilders
                .standaloneSetup(caisseController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(springValidator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .defaultRequest(get("/").accept(MediaType.APPLICATION_JSON))
                .build();

        sampleCaisse = CaisseResponseDTO.builder()
                .id(1L)
                .dateCaisse(LocalDate.now())
                .soldeOuverture(new BigDecimal("10000.00"))
                .soldeCourant(new BigDecimal("9500.00"))
                .agentNom("Alami")
                .agentPrenom("Karim")
                .agenceNom("Agence Casablanca Centre")
                .nombreOperations(5)
                .totalEncaissements(new BigDecimal("2000.00"))
                .totalDecaissements(new BigDecimal("500.00"))
                .build();
    }

    // Helper method pour les tests qui ont besoin d'authentification
    private void setupSecurityContext(String username) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);
    }

    // ── GET /api/v1/agent/cash-register ──────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/agent/cash-register — 200 OK avec caisse courante")
    void getCaisseCourante_shouldReturn200() throws Exception {
        setupSecurityContext(testUsername);
        when(caisseService.getCaisseCourante(testUsername)).thenReturn(sampleCaisse);

        mockMvc.perform(get("/api/v1/agent/cash-register"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.agentNom").value("Alami"))
                .andExpect(jsonPath("$.agentPrenom").value("Karim"))
                .andExpect(jsonPath("$.soldeCourant").value(9500.00));
    }

    @Test
    @DisplayName("GET /api/v1/agent/cash-register — service retourne null → 200")
    void getCaisseCourante_shouldReturn200_whenServiceReturnsNull() throws Exception {
        setupSecurityContext(testUsername);
        when(caisseService.getCaisseCourante(testUsername)).thenReturn(null);

        mockMvc.perform(get("/api/v1/agent/cash-register"))
                .andExpect(status().isOk());
    }

    // ── GET /api/v1/agent/cash-register/operations ───────────────────────────

    @Test
    @DisplayName("GET /api/v1/agent/cash-register/operations — 200 OK opérations du jour")
    void getOperationsDuJour_shouldReturn200() throws Exception {
        setupSecurityContext(testUsername);
        when(caisseService.getOperationsDuJour(testUsername)).thenReturn(sampleCaisse);

        mockMvc.perform(get("/api/v1/agent/cash-register/operations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreOperations").value(5))
                .andExpect(jsonPath("$.totalEncaissements").value(2000.00))
                .andExpect(jsonPath("$.totalDecaissements").value(500.00));

        verify(caisseService).getOperationsDuJour(testUsername);
    }

    @Test
    @DisplayName("GET /api/v1/agent/cash-register/operations — aucune opération → 200")
    void getOperationsDuJour_shouldReturn200_whenNoOperations() throws Exception {
        setupSecurityContext(testUsername);

        CaisseResponseDTO empty = CaisseResponseDTO.builder()
                .nombreOperations(0)
                .totalEncaissements(BigDecimal.ZERO)
                .totalDecaissements(BigDecimal.ZERO)
                .build();

        when(caisseService.getOperationsDuJour(testUsername)).thenReturn(empty);

        mockMvc.perform(get("/api/v1/agent/cash-register/operations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreOperations").value(0))
                .andExpect(jsonPath("$.totalEncaissements").value(0))
                .andExpect(jsonPath("$.totalDecaissements").value(0));
    }

    // ── POST /api/v1/agent/cash-register/close ───────────────────────────────

    @Test
    @DisplayName("POST /api/v1/agent/cash-register/close — 200 OK clôture réussie")
    void cloturerCaisse_shouldReturn200() throws Exception {
        setupSecurityContext(testUsername);

        ClotureCaisseRequestDTO request = new ClotureCaisseRequestDTO();
        request.setSoldeCompte(new BigDecimal("9500.00"));
        request.setObservation("Clôture fin de journée");

        when(caisseService.cloturerCaisse(eq(testUsername), any(ClotureCaisseRequestDTO.class)))
                .thenReturn(sampleCaisse);

        mockMvc.perform(post("/api/v1/agent/cash-register/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.soldeCourant").value(9500.00));

        verify(caisseService).cloturerCaisse(eq(testUsername), any(ClotureCaisseRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/agent/cash-register/close — 400 body vide (@Valid)")
    void cloturerCaisse_shouldReturn400_whenBodyInvalid() throws Exception {
        // PAS de setupSecurityContext - la validation échoue avant l'authentification
        mockMvc.perform(post("/api/v1/agent/cash-register/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(caisseService);
    }

    @Test
    @DisplayName("POST /api/v1/agent/cash-register/close — solde négatif → 400")
    void cloturerCaisse_shouldReturn400_whenSoldeNegatif() throws Exception {
        // PAS de setupSecurityContext - la validation échoue avant l'authentification
        ClotureCaisseRequestDTO request = new ClotureCaisseRequestDTO();
        request.setSoldeCompte(new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/v1/agent/cash-register/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/agent/cash-register/close — Content-Type manquant → 415")
    void cloturerCaisse_shouldReturn415_whenContentTypeMissing() throws Exception {
        mockMvc.perform(post("/api/v1/agent/cash-register/close")
                        .content("{\"soldeCompte\":9500}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ── POST /api/v1/agent/cash-register/discrepancy ─────────────────────────

    @Test
    @DisplayName("POST /api/v1/agent/cash-register/discrepancy — 204 écart signalé")
    void signalerEcart_shouldReturn204() throws Exception {
        setupSecurityContext(testUsername);

        EcartCaisseRequestDTO request = new EcartCaisseRequestDTO();
        request.setMontantEcart(new BigDecimal("150.00"));
        request.setMotif("Erreur de rendu monnaie");

        doNothing().when(caisseService).signalerEcart(eq(testUsername), any(EcartCaisseRequestDTO.class));

        mockMvc.perform(post("/api/v1/agent/cash-register/discrepancy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(caisseService).signalerEcart(eq(testUsername), any(EcartCaisseRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/agent/cash-register/discrepancy — 400 body vide (@Valid)")
    void signalerEcart_shouldReturn400_whenBodyInvalid() throws Exception {
        // PAS de setupSecurityContext - la validation échoue avant l'authentification
        mockMvc.perform(post("/api/v1/agent/cash-register/discrepancy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(caisseService);
    }

    @Test
    @DisplayName("POST /api/v1/agent/cash-register/discrepancy — motif vide → 400")
    void signalerEcart_shouldReturn400_whenMotifBlank() throws Exception {
        // PAS de setupSecurityContext - la validation échoue avant l'authentification
        EcartCaisseRequestDTO request = new EcartCaisseRequestDTO();
        request.setMontantEcart(new BigDecimal("150.00"));
        request.setMotif("");

        mockMvc.perform(post("/api/v1/agent/cash-register/discrepancy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/agent/cash-register/discrepancy — Content-Type manquant → 415")
    void signalerEcart_shouldReturn415_whenContentTypeMissing() throws Exception {
        mockMvc.perform(post("/api/v1/agent/cash-register/discrepancy")
                        .content("{\"montantEcart\":150,\"motif\":\"Test\"}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ── GET /api/v1/manager/cash-registers ───────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/manager/cash-registers — 200 OK liste des caisses agence")
    void getCaissesAgence_shouldReturn200() throws Exception {
        setupSecurityContext(managerUsername);
        when(caisseService.getCaissesAgence(managerUsername)).thenReturn(List.of(sampleCaisse));

        mockMvc.perform(get("/api/v1/manager/cash-registers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].agentNom").value("Alami"))
                .andExpect(jsonPath("$[0].soldeCourant").value(9500.00));

        verify(caisseService).getCaissesAgence(managerUsername);
    }

    @Test
    @DisplayName("GET /api/v1/manager/cash-registers — liste vide → 200 OK")
    void getCaissesAgence_shouldReturn200_whenEmptyList() throws Exception {
        setupSecurityContext(managerUsername);
        when(caisseService.getCaissesAgence(managerUsername)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/manager/cash-registers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/manager/cash-registers — plusieurs caisses → 200 OK")
    void getCaissesAgence_shouldReturn200_whenMultipleCaisses() throws Exception {
        setupSecurityContext(managerUsername);

        CaisseResponseDTO caisse2 = CaisseResponseDTO.builder()
                .id(2L)
                .agentNom("Rachidi")
                .agentPrenom("Sara")
                .soldeCourant(new BigDecimal("8000.00"))
                .build();

        when(caisseService.getCaissesAgence(managerUsername)).thenReturn(List.of(sampleCaisse, caisse2));

        mockMvc.perform(get("/api/v1/manager/cash-registers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].agentNom").value("Rachidi"))
                .andExpect(jsonPath("$[1].soldeCourant").value(8000.00));
    }
}