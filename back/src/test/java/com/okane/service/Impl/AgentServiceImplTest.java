package com.okane.service.impl;

import com.okane.dto.converter.AgentConverter;
import com.okane.dto.requestDto.AssignAgentRequestDto;
import com.okane.dto.requestDto.UpdateAgentStatusRequestDto;
import com.okane.dto.responseDto.AgentDetailResponseDto;
import com.okane.entity.Agence;
import com.okane.entity.User;
import com.okane.entity.enums.Role;
import com.okane.repository.AgenceRepository;
import com.okane.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentServiceImplTest {

    @Mock private UserRepository   userRepository;
    @Mock private AgenceRepository agenceRepository;
    @Mock private AgentConverter   agentConverter;

    @InjectMocks
    private AgentServiceImpl agentService;

    private Agence agence;
    private User   agent;
    private AgentDetailResponseDto agentDto;

    @BeforeEach
    void setUp() {
        agence = Agence.builder()
                .id(1L)
                .nom("Agence Casa")
                .ville("Casablanca")
                .build();

        agent = User.builder()
                .id(10L)
                .email("agent@okane.com")
                .nom("Doe")
                .prenom("John")
                .role(Role.AGENT)
                .active(true)
                .deleted(false)
                .agence(agence)
                .build();

        agentDto = AgentDetailResponseDto.builder()
                .id(10L)
                .nom("Doe")
                .prenom("John")
                .email("agent@okane.com")
                .role(Role.AGENT)
                .active(true)
                .agenceId(1L)
                .agenceNom("Agence Casa")
                .agenceVille("Casablanca")
                .caisseOuverte(false)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // getAgentsByAgence()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getAgentsByAgence_shouldReturnAgentsWhenAgenceExists() {
        when(agenceRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findByAgenceIdAndRoleAndDeletedFalse(1L, Role.AGENT))
                .thenReturn(List.of(agent));
        when(agentConverter.toDto(agent)).thenReturn(agentDto);

        List<AgentDetailResponseDto> result = agentService.getAgentsByAgence(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        verify(agenceRepository).existsById(1L);
        verify(userRepository).findByAgenceIdAndRoleAndDeletedFalse(1L, Role.AGENT);
    }

    @Test
    void getAgentsByAgence_shouldReturnEmptyListWhenNoAgents() {
        when(agenceRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findByAgenceIdAndRoleAndDeletedFalse(1L, Role.AGENT))
                .thenReturn(List.of());

        List<AgentDetailResponseDto> result = agentService.getAgentsByAgence(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(agentConverter, never()).toDto(any());
    }

    @Test
    void getAgentsByAgence_shouldThrowWhenAgenceNotFound() {
        when(agenceRepository.existsById(99L)).thenReturn(false);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> agentService.getAgentsByAgence(99L));

        assertTrue(ex.getMessage().contains("99"));
        verify(userRepository, never()).findByAgenceIdAndRoleAndDeletedFalse(any(), any());
    }

    @Test
    void getAgentsByAgence_shouldMapAllAgentsToDto() {
        User agent2 = User.builder()
                .id(11L).email("agent2@okane.com")
                .nom("Smith").prenom("Jane")
                .role(Role.AGENT).active(true).deleted(false)
                .agence(agence).build();

        AgentDetailResponseDto dto2 = AgentDetailResponseDto.builder()
                .id(11L).email("agent2@okane.com").build();

        when(agenceRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findByAgenceIdAndRoleAndDeletedFalse(1L, Role.AGENT))
                .thenReturn(List.of(agent, agent2));
        when(agentConverter.toDto(agent)).thenReturn(agentDto);
        when(agentConverter.toDto(agent2)).thenReturn(dto2);

        List<AgentDetailResponseDto> result = agentService.getAgentsByAgence(1L);

        assertEquals(2, result.size());
        verify(agentConverter, times(2)).toDto(any(User.class));
    }

    // ─────────────────────────────────────────────────────────────
    // assignAgent()
    // ─────────────────────────────────────────────────────────────

    @Test
    void assignAgent_shouldAssignAgentToAgenceSuccessfully() {
        // agent sans agence actuelle
        User agentSansAgence = User.builder()
                .id(10L).email("agent@okane.com")
                .nom("Doe").prenom("John")
                .role(Role.AGENT).active(true).deleted(false)
                .agence(null)
                .build();

        AssignAgentRequestDto request = AssignAgentRequestDto.builder()
                .userId(10L).build();

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(userRepository.findById(10L)).thenReturn(Optional.of(agentSansAgence));
        when(userRepository.save(agentSansAgence)).thenReturn(agentSansAgence);
        when(agentConverter.toDto(agentSansAgence)).thenReturn(agentDto);

        AgentDetailResponseDto result = agentService.assignAgent(1L, request);

        assertNotNull(result);
        assertEquals(agence, agentSansAgence.getAgence());
        verify(userRepository).save(agentSansAgence);
    }

    @Test
    void assignAgent_shouldAllowReassignToAnotherAgence() {
        // agent déjà dans une autre agence (id=2)
        Agence autreAgence = Agence.builder().id(2L).nom("Agence Rabat").build();
        User agentAutreAgence = User.builder()
                .id(10L).email("agent@okane.com")
                .role(Role.AGENT).active(true).deleted(false)
                .agence(autreAgence)
                .build();

        AssignAgentRequestDto request = AssignAgentRequestDto.builder().userId(10L).build();

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(userRepository.findById(10L)).thenReturn(Optional.of(agentAutreAgence));
        when(userRepository.save(agentAutreAgence)).thenReturn(agentAutreAgence);
        when(agentConverter.toDto(agentAutreAgence)).thenReturn(agentDto);

        AgentDetailResponseDto result = agentService.assignAgent(1L, request);

        assertNotNull(result);
        assertEquals(agence, agentAutreAgence.getAgence());
    }

    @Test
    void assignAgent_shouldThrowWhenAgenceNotFound() {
        AssignAgentRequestDto request = AssignAgentRequestDto.builder().userId(10L).build();

        when(agenceRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> agentService.assignAgent(99L, request));

        assertTrue(ex.getMessage().contains("99"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void assignAgent_shouldThrowWhenUserNotFound() {
        AssignAgentRequestDto request = AssignAgentRequestDto.builder().userId(99L).build();

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> agentService.assignAgent(1L, request));

        assertTrue(ex.getMessage().contains("99"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void assignAgent_shouldThrowWhenUserIsNotAgent() {
        User client = User.builder()
                .id(10L).role(Role.CLIENT).deleted(false).agence(null).build();

        AssignAgentRequestDto request = AssignAgentRequestDto.builder().userId(10L).build();

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(userRepository.findById(10L)).thenReturn(Optional.of(client));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> agentService.assignAgent(1L, request));

        assertTrue(ex.getMessage().contains("AGENT"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void assignAgent_shouldThrowWhenUserIsDeleted() {
        User deletedAgent = User.builder()
                .id(10L).role(Role.AGENT).deleted(true).agence(null).build();

        AssignAgentRequestDto request = AssignAgentRequestDto.builder().userId(10L).build();

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(userRepository.findById(10L)).thenReturn(Optional.of(deletedAgent));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> agentService.assignAgent(1L, request));

        assertTrue(ex.getMessage().contains("supprimé"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void assignAgent_shouldThrowWhenAgentAlreadyInSameAgence() {
        // agent.agence.id == agenceId → déjà affecté
        AssignAgentRequestDto request = AssignAgentRequestDto.builder().userId(10L).build();

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(userRepository.findById(10L)).thenReturn(Optional.of(agent)); // agent.agence.id=1

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> agentService.assignAgent(1L, request));

        assertTrue(ex.getMessage().contains("déjà affecté"));
        verify(userRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // removeAgent()
    // ─────────────────────────────────────────────────────────────

    @Test
    void removeAgent_shouldDetachAgentFromAgence() {
        when(agenceRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(agent));
        when(userRepository.existsByIdAndAgenceId(10L, 1L)).thenReturn(true);

        agentService.removeAgent(1L, 10L);

        assertNull(agent.getAgence());
        verify(userRepository).save(agent);
    }

    @Test
    void removeAgent_shouldThrowWhenAgenceNotFound() {
        when(agenceRepository.existsById(99L)).thenReturn(false);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> agentService.removeAgent(99L, 10L));

        assertTrue(ex.getMessage().contains("99"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void removeAgent_shouldThrowWhenUserNotFound() {
        when(agenceRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> agentService.removeAgent(1L, 99L));

        assertTrue(ex.getMessage().contains("99"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void removeAgent_shouldThrowWhenAgentDoesNotBelongToAgence() {
        when(agenceRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(agent));
        when(userRepository.existsByIdAndAgenceId(10L, 1L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> agentService.removeAgent(1L, 10L));

        assertTrue(ex.getMessage().contains("n'appartient pas"));
        verify(userRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // updateAgentStatus()
    // ─────────────────────────────────────────────────────────────

    @Test
    void updateAgentStatus_shouldActivateAgent() {
        agent.setActive(false);
        UpdateAgentStatusRequestDto request = UpdateAgentStatusRequestDto.builder()
                .active(true).build();

        AgentDetailResponseDto activeDto = AgentDetailResponseDto.builder()
                .id(10L).active(true).build();

        when(agenceRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(agent));
        when(userRepository.existsByIdAndAgenceId(10L, 1L)).thenReturn(true);
        when(userRepository.save(agent)).thenReturn(agent);
        when(agentConverter.toDto(agent)).thenReturn(activeDto);

        AgentDetailResponseDto result = agentService.updateAgentStatus(1L, 10L, request);

        assertTrue(result.getActive());
        assertTrue(agent.getActive());
        verify(userRepository).save(agent);
    }

    @Test
    void updateAgentStatus_shouldSuspendAgent() {
        UpdateAgentStatusRequestDto request = UpdateAgentStatusRequestDto.builder()
                .active(false).build();

        AgentDetailResponseDto suspendedDto = AgentDetailResponseDto.builder()
                .id(10L).active(false).build();

        when(agenceRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(agent));
        when(userRepository.existsByIdAndAgenceId(10L, 1L)).thenReturn(true);
        when(userRepository.save(agent)).thenReturn(agent);
        when(agentConverter.toDto(agent)).thenReturn(suspendedDto);

        AgentDetailResponseDto result = agentService.updateAgentStatus(1L, 10L, request);

        assertFalse(result.getActive());
        assertFalse(agent.getActive());
    }

    @Test
    void updateAgentStatus_shouldThrowWhenAgenceNotFound() {
        UpdateAgentStatusRequestDto request = UpdateAgentStatusRequestDto.builder()
                .active(true).build();

        when(agenceRepository.existsById(99L)).thenReturn(false);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> agentService.updateAgentStatus(99L, 10L, request));

        assertTrue(ex.getMessage().contains("99"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateAgentStatus_shouldThrowWhenUserNotFound() {
        UpdateAgentStatusRequestDto request = UpdateAgentStatusRequestDto.builder()
                .active(true).build();

        when(agenceRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> agentService.updateAgentStatus(1L, 99L, request));

        assertTrue(ex.getMessage().contains("99"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateAgentStatus_shouldThrowWhenAgentDoesNotBelongToAgence() {
        UpdateAgentStatusRequestDto request = UpdateAgentStatusRequestDto.builder()
                .active(true).build();

        when(agenceRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(agent));
        when(userRepository.existsByIdAndAgenceId(10L, 1L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> agentService.updateAgentStatus(1L, 10L, request));

        assertTrue(ex.getMessage().contains("n'appartient pas"));
        verify(userRepository, never()).save(any());
    }
}