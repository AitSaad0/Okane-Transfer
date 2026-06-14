package com.okane.controller;

import com.okane.dto.requestDto.ForceCancelRequestDTO;
import com.okane.dto.responseDto.TransfertResponseDTO;
import com.okane.pagination.PageResponseDto;
import com.okane.service.ClientTransfertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@Tag(name = "Admin — Supervision Transferts", description = "Vue consolidée de tous les transferts")
public class AdminTransfertController {

    private final ClientTransfertService clientTransfertService;

    public AdminTransfertController(ClientTransfertService clientTransfertService) {
        this.clientTransfertService = clientTransfertService;
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }

    @GetMapping("/api/v1/admin/transfers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Liste consolidée des transferts (corridor, statut, date, agence)")
    public ResponseEntity<PageResponseDto<TransfertResponseDTO>> getAllTransferts(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) Long agenceId,
            @RequestParam(required = false) Long corridorId,
            @RequestParam(required = false) String debut,
            @RequestParam(required = false) String fin,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        PageResponseDto<TransfertResponseDTO> result =
                clientTransfertService.getAllTransfertsAdmin(statut, agenceId, corridorId, debut, fin, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/v1/admin/transfers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Détail complet d'un transfert")
    public ResponseEntity<TransfertResponseDTO> getTransfertById(@PathVariable Long id) {
        return ResponseEntity.ok(clientTransfertService.getTransfertAdminById(id));
    }

    // Dans AdminTransfertController.java, la méthode forceCancel doit être :
    @PostMapping("/api/v1/admin/transfers/{id}/force-cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> forceCancel(
            @PathVariable Long id,
            @Valid @RequestBody ForceCancelRequestDTO request,
            Principal principal) {  // ← AJOUTER CE PARAMÈTRE

        String email = principal.getName();  // ← Utiliser principal directement
        clientTransfertService.forceCancelTransfert(id, request, email);
        return ResponseEntity.noContent().build();
    }
}