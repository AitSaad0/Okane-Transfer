package com.okane.repository;

import com.okane.entity.Pays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaysRepository extends JpaRepository<Pays, Long> {
    boolean existsByCodeIso(String codeIso);
    Optional<Pays> findByCodeIso(String codeIso);
}
