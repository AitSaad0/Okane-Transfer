package com.okane.repository;

import com.okane.entity.TauxChangeHistorique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TauxChangeHistoriqueRepository extends JpaRepository<TauxChangeHistorique, Long> {
    List<TauxChangeHistorique> findByCorridorIdOrderByDateChangementDesc(Long corridorId);
}