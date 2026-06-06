package com.okane.controller;

import com.okane.dto.responseDto.JournalAuditResponseDTO;
import com.okane.dto.responseDto.PageResponseDTO;
import com.okane.security.Roles;
import com.okane.service.JournalAuditService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@PreAuthorize(Roles.ADMIN)
public class AuditLogController {

    private final JournalAuditService journalAuditService;

    public AuditLogController(JournalAuditService journalAuditService) {
        this.journalAuditService = journalAuditService;
    }

    // GET /api/v1/admin/audit-logs?page=0&size=20&sort=timestamp&dir=desc
    @GetMapping
    public ResponseEntity<PageResponseDTO<JournalAuditResponseDTO>> getAll(
            @RequestParam(defaultValue = "0")         int    page,
            @RequestParam(defaultValue = "20")        int    size,
            @RequestParam(defaultValue = "timestamp") String sort,
            @RequestParam(defaultValue = "desc")      String dir
    ) {
        int safeSize = Math.min(size, 100);

        Sort.Direction direction = "asc".equalsIgnoreCase(dir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(direction, sort));

        return ResponseEntity.ok(PageResponseDTO.from(journalAuditService.findAll(pageable)));
    }

    // GET /api/v1/admin/audit-logs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<JournalAuditResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(journalAuditService.findById(id));
    }
}