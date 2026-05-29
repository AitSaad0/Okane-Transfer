package com.okane.repository;

import com.okane.entity.GrilleTarifaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface GrilleTarifaireRepository extends JpaRepository<GrilleTarifaire, Long> {
    List<GrilleTarifaire> findByCorridorId(Long corridorId);
    Optional<GrilleTarifaire> findByCorridorIdAndMontantMinLessThanEqualAndMontantMaxGreaterThanEqual(
            Long corridorId, BigDecimal montant, BigDecimal montant2);
}