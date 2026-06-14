package com.okane.controller;

import com.okane.dto.requestDto.ClotureCaisseRequestDTO;
import com.okane.dto.requestDto.EcartCaisseRequestDTO;
import com.okane.dto.responseDto.CaisseResponseDTO;
import com.okane.service.CaisseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Agent — Caisse", description = "Gestion de la caisse agent et supervision manager")
public class CaisseController {

    private final CaisseService caisseService;

    public CaisseController(CaisseService caisseService) {
        this.caisseService = caisseService;
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @GetMapping(value = "/api/v1/agent/cash-register", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('AGENT')")
    @Operation(summary = "Solde et état actuel de la caisse de l'agent connecté")
    public ResponseEntity<CaisseResponseDTO> getCaisseCourante() {
        String email = getCurrentUserEmail();
        CaisseResponseDTO result = caisseService.getCaisseCourante(email);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/api/v1/agent/cash-register/operations", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('AGENT')")
    @Operation(summary = "Historique détaillé des opérations de la journée")
    public ResponseEntity<CaisseResponseDTO> getOperationsDuJour() {
        String email = getCurrentUserEmail();
        CaisseResponseDTO result = caisseService.getOperationsDuJour(email);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/api/v1/agent/cash-register/close",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('AGENT')")
    @Operation(summary = "Clôture de caisse journalière avec réconciliation automatique")
    public ResponseEntity<CaisseResponseDTO> cloturerCaisse(
            @Valid @RequestBody ClotureCaisseRequestDTO request) {
        String email = getCurrentUserEmail();
        CaisseResponseDTO result = caisseService.cloturerCaisse(email, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/api/v1/agent/cash-register/discrepancy",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('AGENT')")
    @Operation(summary = "Signale un écart de caisse avec montant et motif obligatoires")
    public ResponseEntity<Void> signalerEcart(
            @Valid @RequestBody EcartCaisseRequestDTO request) {
        String email = getCurrentUserEmail();
        caisseService.signalerEcart(email, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/api/v1/manager/cash-registers", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Vue des caisses de tous les agents de l'agence (manager)")
    public ResponseEntity<List<CaisseResponseDTO>> getCaissesAgence() {
        String email = getCurrentUserEmail();
        List<CaisseResponseDTO> result = caisseService.getCaissesAgence(email);
        return ResponseEntity.ok(result);
    }
}