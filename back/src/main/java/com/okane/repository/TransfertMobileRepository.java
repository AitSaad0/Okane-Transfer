package com.okane.repository;

import com.okane.entity.TransfertMobile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransfertMobileRepository extends JpaRepository<TransfertMobile, Long> {
    Optional<TransfertMobile> findByTransfertId(Long transfertId);
}
