package com.okane.service;

import com.okane.dto.responseDto.JournalAuditResponseDTO;
import com.okane.entity.JournalAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JournalAuditService {

    /** Persist a new audit entry (called by the AOP aspect). */
    void save(JournalAudit entry);

    /** Return a paginated list of all audit logs — for ADMIN. */
    Page<JournalAuditResponseDTO> findAll(Pageable pageable);

    /** Return a single audit log by id — for ADMIN. */
    JournalAuditResponseDTO findById(Long id);
}