package com.okane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okane.dto.requestDto.CreateAgenceRequestDto;
import com.okane.dto.requestDto.UpdateAgenceRequestDto;
import com.okane.dto.requestDto.UpdateAgenceStatusRequestDto;
import com.okane.dto.responseDto.AgenceDashboardResponseDto;
import com.okane.dto.responseDto.AgenceResponseDto;
import com.okane.entity.enums.StatutAgence;
import com.okane.exception.GlobalExceptionHandler;
import com.okane.exception.ResourceNotFoundException;
import com.okane.pagination.PageResponseDto;
import com.okane.service.AgenceService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminAgenceControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AgenceService agenceService;

    @InjectMocks
    private AdminAgenceController adminAgenceController;

    private AgenceResponseDto sampleAgence;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(adminAgenceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleAgence = AgenceResponseDto.builder()
                .id(1L)
                .nom("Agence Casablanca Centre")
                .adresse("12 Rue Hassan II")
                .ville("Casablanca")
                .codePostal("20000")
                .plafondJournalier(new BigDecimal("50000.00"))
                .statut(StatutAgence.ACTIVE)
                .paysNom("Maroc")
                .paysCode("MA")
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/v1/admin/agencies
    // ─────────────────────────────────────────────────────────────

    @Test
    void getAllAgences_shouldReturn200WithPagedResult() throws Exception {
        PageResponseDto<AgenceResponseDto> page = new PageResponseDto<>(
                List.of(sampleAgence), 0, 20, 1L, 1, true
        );

        when(agenceService.getAllAgences(null, null, 0, 20, "id")).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/agencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].nom").value("Agence Casablanca Centre"))
                .andExpect(jsonPath("$.content[0].ville").value("Casablanca"))
                .andExpect(jsonPath("$.content[0].statut").value("ACTIVE"));
    }

    @Test
    void getAllAgences_shouldReturn200FilteredByPaysAndStatut() throws Exception {
        PageResponseDto<AgenceResponseDto> page = new PageResponseDto<>(
                List.of(sampleAgence), 0, 20, 1L, 1, true
        );

        when(agenceService.getAllAgences(eq(1L), eq(StatutAgence.ACTIVE), eq(0), eq(20), eq("id")))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/agencies")
                        .param("paysId", "1")
                        .param("statut", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].statut").value("ACTIVE"));
    }

    @Test
    void getAllAgences_shouldReturn200FilteredByStatutSuspendue() throws Exception {
        AgenceResponseDto suspendue = AgenceResponseDto.builder()
                .id(2L)
                .nom("Agence Rabat")
                .statut(StatutAgence.SUSPENDUE)
                .build();

        PageResponseDto<AgenceResponseDto> page = new PageResponseDto<>(
                List.of(sampleAgence), 0, 20, 1L, 1, true
        );

        when(agenceService.getAllAgences(null, eq(StatutAgence.SUSPENDUE), eq(0), eq(20), eq("id")))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/agencies")
                        .param("statut", "SUSPENDUE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].statut").value("SUSPENDUE"));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/v1/admin/agencies/{id}
    // ─────────────────────────────────────────────────────────────

    @Test
    void getAgenceById_shouldReturn200WithAllFields() throws Exception {
        when(agenceService.getAgenceById(1L)).thenReturn(sampleAgence);

        mockMvc.perform(get("/api/v1/admin/agencies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nom").value("Agence Casablanca Centre"))
                .andExpect(jsonPath("$.adresse").value("12 Rue Hassan II"))
                .andExpect(jsonPath("$.ville").value("Casablanca"))
                .andExpect(jsonPath("$.codePostal").value("20000"))
                .andExpect(jsonPath("$.statut").value("ACTIVE"))
                .andExpect(jsonPath("$.paysNom").value("Maroc"))
                .andExpect(jsonPath("$.paysCode").value("MA"));
    }

    @Test
    void getAgenceById_shouldReturn404WhenNotFound() throws Exception {
        when(agenceService.getAgenceById(99L))
                .thenThrow(new ResourceNotFoundException("Agence introuvable"));

        mockMvc.perform(get("/api/v1/admin/agencies/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Agence introuvable"));
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/v1/admin/agencies
    // ─────────────────────────────────────────────────────────────

    @Test
    void createAgence_shouldReturn201WithCreatedAgence() throws Exception {
        CreateAgenceRequestDto request = CreateAgenceRequestDto.builder()
                .nom("Agence Casablanca Centre")
                .adresse("12 Rue Hassan II")
                .ville("Casablanca")
                .codePostal("20000")
                .plafondJournalier(new BigDecimal("50000.00"))
                .paysId(1L)
                .build();

        when(agenceService.createAgence(any())).thenReturn(sampleAgence);

        mockMvc.perform(post("/api/v1/admin/agencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nom").value("Agence Casablanca Centre"))
                .andExpect(jsonPath("$.statut").value("ACTIVE"));
    }

    @Test
    void createAgence_shouldReturn400WhenNomIsBlank() throws Exception {
        CreateAgenceRequestDto request = CreateAgenceRequestDto.builder()
                .nom("")                               // @NotBlank violated
                .adresse("12 Rue Hassan II")
                .ville("Casablanca")
                .codePostal("20000")
                .plafondJournalier(new BigDecimal("50000.00"))
                .paysId(1L)
                .build();

        mockMvc.perform(post("/api/v1/admin/agencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createAgence_shouldReturn400WhenPaysIdIsNull() throws Exception {
        CreateAgenceRequestDto request = CreateAgenceRequestDto.builder()
                .nom("Agence Casablanca Centre")
                .adresse("12 Rue Hassan II")
                .ville("Casablanca")
                .codePostal("20000")
                .plafondJournalier(new BigDecimal("50000.00"))
                .paysId(null)                         // @NotNull violated
                .build();

        mockMvc.perform(post("/api/v1/admin/agencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAgence_shouldReturn400WhenPlafondIsNegative() throws Exception {
        CreateAgenceRequestDto request = CreateAgenceRequestDto.builder()
                .nom("Agence Casablanca Centre")
                .adresse("12 Rue Hassan II")
                .ville("Casablanca")
                .codePostal("20000")
                .plafondJournalier(new BigDecimal("-100.00")) // @Positive violated
                .paysId(1L)
                .build();

        mockMvc.perform(post("/api/v1/admin/agencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /api/v1/admin/agencies/{id}
    // ─────────────────────────────────────────────────────────────

    @Test
    void updateAgence_shouldReturn200WithUpdatedAgence() throws Exception {
        UpdateAgenceRequestDto request = UpdateAgenceRequestDto.builder()
                .nom("Agence Lyon Sud")
                .adresse("5 Avenue Jean Jaurès")
                .ville("Lyon")
                .codePostal("69007")
                .plafondJournalier(new BigDecimal("80000.00"))
                .paysId(2L)
                .build();

        AgenceResponseDto updated = AgenceResponseDto.builder()
                .id(1L)
                .nom("Agence Lyon Sud")
                .adresse("5 Avenue Jean Jaurès")
                .ville("Lyon")
                .codePostal("69007")
                .plafondJournalier(new BigDecimal("80000.00"))
                .statut(StatutAgence.ACTIVE)
                .build();

        when(agenceService.updateAgence(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/admin/agencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Agence Lyon Sud"))
                .andExpect(jsonPath("$.ville").value("Lyon"))
                .andExpect(jsonPath("$.codePostal").value("69007"));
    }

    @Test
    void updateAgence_shouldReturn400WhenBodyIsInvalid() throws Exception {
        UpdateAgenceRequestDto request = UpdateAgenceRequestDto.builder()
                .nom("")          // @NotBlank violated
                .adresse("")      // @NotBlank violated
                .ville("Lyon")
                .codePostal("69007")
                .plafondJournalier(new BigDecimal("80000.00"))
                .paysId(2L)
                .build();

        mockMvc.perform(put("/api/v1/admin/agencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAgence_shouldReturn404WhenNotFound() throws Exception {
        UpdateAgenceRequestDto request = UpdateAgenceRequestDto.builder()
                .nom("Agence Fantome")
                .adresse("Rue Inconnue")
                .ville("Nulle Part")
                .codePostal("00000")
                .plafondJournalier(new BigDecimal("10000.00"))
                .paysId(1L)
                .build();

        when(agenceService.updateAgence(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Agence introuvable"));

        mockMvc.perform(put("/api/v1/admin/agencies/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Agence introuvable"));
    }

    // ─────────────────────────────────────────────────────────────
    // PATCH /api/v1/admin/agencies/{id}/status
    // ─────────────────────────────────────────────────────────────

    @Test
    void updateAgenceStatus_shouldReturn200WhenSuspended() throws Exception {
        UpdateAgenceStatusRequestDto request = UpdateAgenceStatusRequestDto.builder()
                .statut(StatutAgence.SUSPENDUE)
                .build();

        AgenceResponseDto suspended = AgenceResponseDto.builder()
                .id(1L)
                .nom("Agence Casablanca Centre")
                .statut(StatutAgence.SUSPENDUE)
                .build();

        when(agenceService.updateAgenceStatus(eq(1L), any())).thenReturn(suspended);

        mockMvc.perform(patch("/api/v1/admin/agencies/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("SUSPENDUE"));
    }

    @Test
    void updateAgenceStatus_shouldReturn200WhenReactivated() throws Exception {
        UpdateAgenceStatusRequestDto request = UpdateAgenceStatusRequestDto.builder()
                .statut(StatutAgence.ACTIVE)
                .build();

        when(agenceService.updateAgenceStatus(eq(1L), any())).thenReturn(sampleAgence);

        mockMvc.perform(patch("/api/v1/admin/agencies/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ACTIVE"));
    }

    @Test
    void updateAgenceStatus_shouldReturn400WhenStatutIsNull() throws Exception {
        // statut null → @NotNull violated
        String body = "{\"statut\": null}";

        mockMvc.perform(patch("/api/v1/admin/agencies/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAgenceStatus_shouldReturn404WhenNotFound() throws Exception {
        UpdateAgenceStatusRequestDto request = UpdateAgenceStatusRequestDto.builder()
                .statut(StatutAgence.SUSPENDUE)
                .build();

        when(agenceService.updateAgenceStatus(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Agence introuvable"));

        mockMvc.perform(patch("/api/v1/admin/agencies/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Agence introuvable"));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/v1/admin/agencies/{id}/dashboard
    // ─────────────────────────────────────────────────────────────

    @Test
    void getAgenceDashboard_shouldReturn200WithAllKpis() throws Exception {
        AgenceDashboardResponseDto dashboard = AgenceDashboardResponseDto.builder()
                .agenceId(1L)
                .agenceNom("Agence Casablanca Centre")
                .agenceVille("Casablanca")
                .paysNom("Maroc")
                .volumeEnvoiJour(new BigDecimal("120000.00"))
                .volumePaiementJour(new BigDecimal("95000.00"))
                .nombreTransfertJour(45L)
                .tauxSucces(92.5)
                .transfertsPaye(40L)
                .transfertsEnAttente(3L)
                .transfertsAnnule(2L)
                .transfertsExpire(0L)
                .commissionsGenerees(new BigDecimal("3600.00"))
                .plafondJournalier(new BigDecimal("200000.00"))
                .tauxUtilisationPlafond(60.0)
                .soldeCaisseActuel(new BigDecimal("50000.00"))
                .caisseOuverte(true)
                .build();

        when(agenceService.getAgenceDashboard(1L)).thenReturn(dashboard);

        mockMvc.perform(get("/api/v1/admin/agencies/1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agenceId").value(1L))
                .andExpect(jsonPath("$.agenceNom").value("Agence Casablanca Centre"))
                .andExpect(jsonPath("$.agenceVille").value("Casablanca"))
                .andExpect(jsonPath("$.paysNom").value("Maroc"))
                .andExpect(jsonPath("$.nombreTransfertJour").value(45))
                .andExpect(jsonPath("$.tauxSucces").value(92.5))
                .andExpect(jsonPath("$.transfertsPaye").value(40))
                .andExpect(jsonPath("$.transfertsEnAttente").value(3))
                .andExpect(jsonPath("$.transfertsAnnule").value(2))
                .andExpect(jsonPath("$.transfertsExpire").value(0))
                .andExpect(jsonPath("$.tauxUtilisationPlafond").value(60.0))
                .andExpect(jsonPath("$.caisseOuverte").value(true));
    }

    @Test
    void getAgenceDashboard_shouldReturn404WhenAgenceNotFound() throws Exception {
        when(agenceService.getAgenceDashboard(99L))
                .thenThrow(new ResourceNotFoundException("Agence introuvable"));

        mockMvc.perform(get("/api/v1/admin/agencies/99/dashboard"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Agence introuvable"));
    }
}