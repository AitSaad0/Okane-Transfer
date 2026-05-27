package com.okane.compliance.repository;

import com.okane.compliance.bean.ComplianceThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplianceThresholdRepository
        extends JpaRepository<ComplianceThreshold, Long> {
}