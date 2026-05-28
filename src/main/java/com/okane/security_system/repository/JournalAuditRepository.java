package com.okane.security_system.repository;

import com.okane.security_system.bean.JournalAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long> {
    Page<JournalAudit> findByUtilisateurId(Long utilisateurId, Pageable pageable);
}
