package com.okane.controller;

import com.okane.dto.responseDto.TransfertResponseDTO;
import com.okane.pagination.PageResponseDto;
import com.okane.service.ClientTransfertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated  // ← required for @NotBlank on @RequestParam to trigger
@Tag(name = "Client — Transferts", description = "Suivi des transferts côté client")
public class ClientTransfertController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    private final ClientTransfertService clientTransfertService;

    public ClientTransfertController(ClientTransfertService clientTransfertService) {
        this.clientTransfertService = clientTransfertService;
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return auth.getName();
    }

    /**
     * GET /api/v1/clients/transfers
     *
     * Fix: clamp negative page/size values to defaults instead of letting
     * PageRequest.of() throw IllegalArgumentException → 500.
     */
    @GetMapping("/api/v1/clients/transfers")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENT', 'CLIENT')")
    public ResponseEntity<PageResponseDto<TransfertResponseDTO>> getMyTransferts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        // ← FIX 1: clamp negatives to safe defaults
        int safePage = page < 0 ? DEFAULT_PAGE : page;
        int safeSize = size < 1 ? DEFAULT_SIZE : size;

        String email = getCurrentUserEmail();
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("dateCreation").descending());
        PageResponseDto<TransfertResponseDTO> result =
                clientTransfertService.getTransfertsClient(email, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/v1/clients/transfers/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENT', 'CLIENT')")
    public ResponseEntity<TransfertResponseDTO> getMyTransfertById(@PathVariable Long id) {
        String email = getCurrentUserEmail();
        TransfertResponseDTO result = clientTransfertService.getTransfertClientById(id, email);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/clients/transfers/track?ref=XXXXXXXX
     *
     * Public endpoint — no authentication required.
     * IMPORTANT: declared BEFORE /{id} to avoid being swallowed by the path variable mapping.
     *
     * Fix: @NotBlank triggers a ConstraintViolationException (→ 400) when ref is empty.
     *      MissingServletRequestParameterException (ref absent entirely) is handled by
     *      GlobalExceptionHandler and must also return 400 — see note below.
     */
    @GetMapping("/api/v1/clients/transfers/track")
    @Operation(summary = "Suivi public par numéro de référence (sans authentification)")
    public ResponseEntity<TransfertResponseDTO> trackTransfert(
            @Parameter(description = "Code de retrait alphanumérique à 8 caractères", required = true)
            @RequestParam String ref) {

        if (ref.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Le paramètre 'ref' ne peut pas être vide");
        }

        TransfertResponseDTO result = clientTransfertService.trackTransfert(ref);
        return ResponseEntity.ok(result);
    }
}