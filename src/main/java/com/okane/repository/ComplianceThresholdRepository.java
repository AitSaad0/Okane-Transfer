package com.okane.repository;

import com.okane.entity.ComplianceThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplianceThresholdRepository
        extends JpaRepository<ComplianceThreshold, Long> {
}