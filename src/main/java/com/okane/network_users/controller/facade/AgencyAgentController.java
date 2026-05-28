package com.okane.network_users.controller.facade;

import com.okane.network_users.controller.dto.requestDto.AssignAgentRequestDto;
import com.okane.network_users.controller.dto.requestDto.UpdateAgentStatusRequestDto;
import com.okane.network_users.controller.dto.responseDto.AgentDetailResponseDto;
import com.okane.network_users.service.facade.AgentService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agencies/{agencyId}/agents")
@RequiredArgsConstructor
//@Tag(name = "Agences — Agents", description = "Gestion des agents par agence")
public class AgencyAgentController {

    private final AgentService agentService;

    @GetMapping
    //@Operation(summary = "Liste les agents d'une agence avec leurs détails complets")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<List<AgentDetailResponseDto>> getAgentsByAgence(
            @PathVariable Long agencyId
    ) {
        return ResponseEntity.ok(agentService.getAgentsByAgence(agencyId));
    }

    @PostMapping
    //@Operation(summary = "Affecte un utilisateur AGENT à l'agence")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<AgentDetailResponseDto> assignAgent(
            @PathVariable Long agencyId,
            @Valid @RequestBody AssignAgentRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(agentService.assignAgent(agencyId, request));
    }

    @DeleteMapping("/{userId}")
    //@Operation(summary = "Retire un agent de l'agence")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<Void> removeAgent(
            @PathVariable Long agencyId,
            @PathVariable Long userId
    ) {
        agentService.removeAgent(agencyId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/status")
    //@Operation(summary = "Suspend ou réactive un agent")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<AgentDetailResponseDto> updateAgentStatus(
            @PathVariable Long agencyId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateAgentStatusRequestDto request
    ) {
        return ResponseEntity.ok(agentService.updateAgentStatus(agencyId, userId, request));
    }
}