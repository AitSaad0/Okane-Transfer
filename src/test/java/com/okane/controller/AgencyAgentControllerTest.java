package com.okane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okane.dto.requestDto.AssignAgentRequestDto;
import com.okane.dto.requestDto.UpdateAgentStatusRequestDto;
import com.okane.dto.responseDto.AgentDetailResponseDto;
import com.okane.entity.enums.Role;
import com.okane.exception.GlobalExceptionHandler;
import com.okane.exception.ResourceNotFoundException;
import com.okane.service.AgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AgencyAgentControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules(); // nécessaire pour sérialiser LocalDateTime

    @Mock
    private AgentService agentService;

    @InjectMocks
    private AgencyAgentController agencyAgentController;

    private AgentDetailResponseDto sampleAgent;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(agencyAgentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        sampleAgent = AgentDetailResponseDto.builder()
                .id(1L)
                .nom("Alami")
                .prenom("Youssef")
                .email("youssef.alami@okane.com")
                .telephone("+212600000010")
                .role(Role.AGENT)
                .active(true)
                .agenceId(5L)
                .agenceNom("Agence Casablanca Centre")
                .agenceVille("Casablanca")
                .caisseId("caisse-uuid-001")
                .caisseOuverte(true)
                .soldeCaisse(new BigDecimal("15000.00"))
                .dateOuvertureCaisse(LocalDateTime.of(2026, 5, 30, 8, 0))
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/v1/agencies/{agencyId}/agents
    // ─────────────────────────────────────────────────────────────

    @Test
    void getAgentsByAgence_shouldReturn200WithListOfAgents() throws Exception {
        when(agentService.getAgentsByAgence(5L)).thenReturn(List.of(sampleAgent));

        mockMvc.perform(get("/api/v1/agencies/5/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nom").value("Alami"))
                .andExpect(jsonPath("$[0].prenom").value("Youssef"))
                .andExpect(jsonPath("$[0].email").value("youssef.alami@okane.com"))
                .andExpect(jsonPath("$[0].role").value("AGENT"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0].agenceId").value(5L))
                .andExpect(jsonPath("$[0].agenceNom").value("Agence Casablanca Centre"))
                .andExpect(jsonPath("$[0].caisseOuverte").value(true));
    }

    @Test
    void getAgentsByAgence_shouldReturn200WithEmptyList() throws Exception {
        when(agentService.getAgentsByAgence(5L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/agencies/5/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAgentsByAgence_shouldReturn404WhenAgenceNotFound() throws Exception {
        when(agentService.getAgentsByAgence(99L))
                .thenThrow(new ResourceNotFoundException("Agence introuvable"));

        mockMvc.perform(get("/api/v1/agencies/99/agents"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Agence introuvable"));
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/v1/agencies/{agencyId}/agents
    // ─────────────────────────────────────────────────────────────

    @Test
    void assignAgent_shouldReturn201WithAgentDetails() throws Exception {
        AssignAgentRequestDto request = AssignAgentRequestDto.builder()
                .userId(1L)
                .build();

        when(agentService.assignAgent(eq(5L), any())).thenReturn(sampleAgent);

        mockMvc.perform(post("/api/v1/agencies/5/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nom").value("Alami"))
                .andExpect(jsonPath("$.agenceId").value(5L))
                .andExpect(jsonPath("$.role").value("AGENT"));
    }

    @Test
    void assignAgent_shouldReturn400WhenUserIdIsNull() throws Exception {
        AssignAgentRequestDto request = AssignAgentRequestDto.builder()
                .userId(null)  // @NotNull violated
                .build();

        mockMvc.perform(post("/api/v1/agencies/5/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void assignAgent_shouldReturn404WhenAgenceNotFound() throws Exception {
        AssignAgentRequestDto request = AssignAgentRequestDto.builder()
                .userId(1L)
                .build();

        when(agentService.assignAgent(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Agence introuvable"));

        mockMvc.perform(post("/api/v1/agencies/99/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Agence introuvable"));
    }

    @Test
    void assignAgent_shouldReturn404WhenUserNotFound() throws Exception {
        AssignAgentRequestDto request = AssignAgentRequestDto.builder()
                .userId(999L)
                .build();

        when(agentService.assignAgent(eq(5L), any()))
                .thenThrow(new ResourceNotFoundException("Utilisateur introuvable"));

        mockMvc.perform(post("/api/v1/agencies/5/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Utilisateur introuvable"));
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE /api/v1/agencies/{agencyId}/agents/{userId}
    // ─────────────────────────────────────────────────────────────

    @Test
    void removeAgent_shouldReturn204() throws Exception {
        doNothing().when(agentService).removeAgent(5L, 1L);

        mockMvc.perform(delete("/api/v1/agencies/5/agents/1"))
                .andExpect(status().isNoContent());

        verify(agentService).removeAgent(5L, 1L);
    }

    @Test
    void removeAgent_shouldReturn404WhenAgenceNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Agence introuvable"))
                .when(agentService).removeAgent(99L, 1L);

        mockMvc.perform(delete("/api/v1/agencies/99/agents/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Agence introuvable"));
    }

    @Test
    void removeAgent_shouldReturn404WhenUserNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Utilisateur introuvable"))
                .when(agentService).removeAgent(5L, 999L);

        mockMvc.perform(delete("/api/v1/agencies/5/agents/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Utilisateur introuvable"));
    }

    // ─────────────────────────────────────────────────────────────
    // PATCH /api/v1/agencies/{agencyId}/agents/{userId}/status
    // ─────────────────────────────────────────────────────────────

    @Test
    void updateAgentStatus_shouldReturn200WhenSuspended() throws Exception {
        UpdateAgentStatusRequestDto request = UpdateAgentStatusRequestDto.builder()
                .active(false)
                .build();

        AgentDetailResponseDto suspended = AgentDetailResponseDto.builder()
                .id(1L)
                .nom("Alami")
                .prenom("Youssef")
                .email("youssef.alami@okane.com")
                .role(Role.AGENT)
                .active(false)
                .agenceId(5L)
                .agenceNom("Agence Casablanca Centre")
                .build();

        when(agentService.updateAgentStatus(eq(5L), eq(1L), any())).thenReturn(suspended);

        mockMvc.perform(patch("/api/v1/agencies/5/agents/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateAgentStatus_shouldReturn200WhenReactivated() throws Exception {
        UpdateAgentStatusRequestDto request = UpdateAgentStatusRequestDto.builder()
                .active(true)
                .build();

        when(agentService.updateAgentStatus(eq(5L), eq(1L), any())).thenReturn(sampleAgent);

        mockMvc.perform(patch("/api/v1/agencies/5/agents/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void updateAgentStatus_shouldReturn400WhenActiveIsNull() throws Exception {
        String body = "{\"active\": null}";  // @NotNull violated

        mockMvc.perform(patch("/api/v1/agencies/5/agents/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAgentStatus_shouldReturn404WhenAgenceNotFound() throws Exception {
        UpdateAgentStatusRequestDto request = UpdateAgentStatusRequestDto.builder()
                .active(false)
                .build();

        when(agentService.updateAgentStatus(eq(99L), eq(1L), any()))
                .thenThrow(new ResourceNotFoundException("Agence introuvable"));

        mockMvc.perform(patch("/api/v1/agencies/99/agents/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Agence introuvable"));
    }

    @Test
    void updateAgentStatus_shouldReturn404WhenUserNotFound() throws Exception {
        UpdateAgentStatusRequestDto request = UpdateAgentStatusRequestDto.builder()
                .active(false)
                .build();

        when(agentService.updateAgentStatus(eq(5L), eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Utilisateur introuvable"));

        mockMvc.perform(patch("/api/v1/agencies/5/agents/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Utilisateur introuvable"));
    }
}