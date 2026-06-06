package com.okane.repository;

import com.okane.entity.KycAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface KycAlertRepository extends JpaRepository<KycAlert, UUID> {
    // findById, findAll, save are all inherited from JpaRepository
}