package com.okane.controller;

import com.okane.dto.requestDto.CreateAgenceRequestDto;
import com.okane.dto.requestDto.UpdateAgenceRequestDto;
import com.okane.dto.requestDto.UpdateAgenceStatusRequestDto;
import com.okane.dto.responseDto.AgenceDashboardResponseDto;
import com.okane.dto.responseDto.AgenceResponseDto;
import com.okane.pagination.PageResponseDto;
import com.okane.service.AgenceService;
import com.okane.entity.enums.StatutAgence;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/agencies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
//@Tag(name = "Admin — Agences", description = "CRUD complet des agences")
public class AdminAgenceController {

    private final AgenceService agenceService;

    @GetMapping
    //@Operation(summary = "Liste toutes les agences (filtrables par pays/statut)")
    public ResponseEntity<PageResponseDto<AgenceResponseDto>> getAllAgences(
            @RequestParam(required = false) Long         paysId,
            @RequestParam(required = false) StatutAgence statut,
            @RequestParam(defaultValue = "0")  int      page,
            @RequestParam(defaultValue = "20") int      size,
            @RequestParam(defaultValue = "id") String   sort
    ) {
        return ResponseEntity.ok(
                agenceService.getAllAgences(paysId, statut, page, size, sort)
        );
    }

    @GetMapping("/{id}")
    //@Operation(summary = "Détail complet d'une agence")
    public ResponseEntity<AgenceResponseDto> getAgenceById(@PathVariable Long id) {
        return ResponseEntity.ok(agenceService.getAgenceById(id));
    }

    @PostMapping
    //@Operation(summary = "Crée une nouvelle agence avec ses paramètres")
    public ResponseEntity<AgenceResponseDto> createAgence(
            @Valid @RequestBody CreateAgenceRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(agenceService.createAgence(request));
    }

    @PutMapping("/{id}")
    //@Operation(summary = "Modifie les informations d'une agence")
    public ResponseEntity<AgenceResponseDto> updateAgence(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAgenceRequestDto request
    ) {
        return ResponseEntity.ok(agenceService.updateAgence(id, request));
    }

    @PatchMapping("/{id}/status")
    //@Operation(summary = "Active ou suspend une agence")
    public ResponseEntity<AgenceResponseDto> updateAgenceStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAgenceStatusRequestDto request
    ) {
        return ResponseEntity.ok(agenceService.updateAgenceStatus(id, request));
    }

    @GetMapping("/{id}/dashboard")
    //@Operation(summary = "KPIs de performance de l'agence")
    public ResponseEntity<AgenceDashboardResponseDto> getAgenceDashboard(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(agenceService.getAgenceDashboard(id));
    }
}