package com.okane.controller;

import com.okane.dto.responseDto.ManagerDashboardResponseDTO;
import com.okane.dto.responseDto.TransfertResponseDTO;
import com.okane.pagination.PageResponseDto;
import com.okane.service.ClientTransfertService;
import com.okane.service.ManagerDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Manager — Dashboard & Rapports", description = "Supervision de l'agence par le responsable")
public class ManagerController {

    private final ClientTransfertService clientTransfertService;
    private final ManagerDashboardService managerDashboardService;

    public ManagerController(ClientTransfertService clientTransfertService,
                             ManagerDashboardService managerDashboardService) {
        this.clientTransfertService = clientTransfertService;
        this.managerDashboardService = managerDashboardService;
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @GetMapping("/api/v1/manager/transfers")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Transferts de l'agence du manager connecté")
    public ResponseEntity<PageResponseDto<TransfertResponseDTO>> getTransfertsAgence(
            @RequestParam(required = false) String statut,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        String email = getCurrentUserEmail();
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        PageResponseDto<TransfertResponseDTO> result =
                clientTransfertService.getTransfertsManager(email, statut, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/v1/manager/dashboard")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "KPIs temps réel de l'agence du manager connecté")
    public ResponseEntity<ManagerDashboardResponseDTO> getDashboard() {
        String email = getCurrentUserEmail();
        ManagerDashboardResponseDTO result = managerDashboardService.getDashboard(email);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/v1/manager/reports/daily")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Rapport journalier de l'agence (par défaut : aujourd'hui)")
    public ResponseEntity<ManagerDashboardResponseDTO> getRapportJournalier(
            @RequestParam(required = false) String date) {
        String email = getCurrentUserEmail();
        ManagerDashboardResponseDTO result =
                managerDashboardService.getRapportJournalier(email, date);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/v1/manager/reports/export")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Export PDF/CSV du rapport agence")
    public ResponseEntity<byte[]> exportRapport(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String date) {
        String email = getCurrentUserEmail();
        byte[] content = managerDashboardService.exportRapport(email, format, date);

        MediaType mediaType = "pdf".equalsIgnoreCase(format)
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("text/csv;charset=UTF-8");

        String filename = "rapport_agence_" + (date != null ? date : "today") + "." + format.toLowerCase();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(content);
    }
}