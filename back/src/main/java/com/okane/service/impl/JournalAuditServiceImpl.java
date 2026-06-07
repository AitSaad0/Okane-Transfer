package com.okane.service.impl;

import com.okane.dto.responseDto.JournalAuditResponseDTO;
import com.okane.entity.JournalAudit;
import com.okane.exception.ResourceNotFoundException;
import com.okane.repository.JournalAuditRepository;
import com.okane.service.JournalAuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JournalAuditServiceImpl implements JournalAuditService {

    private final JournalAuditRepository repository;

    public JournalAuditServiceImpl(JournalAuditRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(JournalAudit entry) {
        repository.save(entry);
    }

    // ------------------------------------------------------------------ //
    //  Read — used by the admin controller
    // ------------------------------------------------------------------ //

    @Override
    @Transactional(readOnly = true)
    public Page<JournalAuditResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                         .map(JournalAuditResponseDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public JournalAuditResponseDTO findById(Long id) {
        JournalAudit entry = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("JournalAudit not found with id: " + id));
        return JournalAuditResponseDTO.from(entry);
    }
}