package com.okane.repository;

import com.okane.entity.Caisse;
import com.okane.entity.User;
import com.okane.entity.enums.StatutCaisse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * FIXES APPLIED:
 *
 * 1. Method name mismatch — CaisseServiceImpl calls:
 *      caisseRepository.findByAgentAndDateCaisseAndStatut(agent, LocalDate.now(), StatutCaisse.OUVERTE)
 *    but the original repo only had:
 *      findByAgentAndDateOuvertureAndStatut(agent, LocalDate, StatutCaisse)
 *    which queries on dateOuverture (a LocalDateTime), not on the dateCaisse (LocalDate) column.
 *    FIX: renamed to findByAgentAndDateCaisseAndStatut and query now uses c.dateCaisse.
 *
 * 2. Method name mismatch — CaisseServiceImpl calls:
 *      caisseRepository.findByAgenceIdAndDate(agenceId, LocalDate.now())
 *    but the original repo only had:
 *      findByAgenceIdAndDateOuverture(agenceId, LocalDate)
 *    FIX: renamed to findByAgenceIdAndDate and query now uses c.dateCaisse.
 *
 * 3. Kept findByAgentOrderByDateOuvertureDesc and findByAgentAndStatut as-is (not used in
 *    CaisseServiceImpl directly, but useful for history and open-caisse lookups).
 *
 * 4. Removed unused LocalDateTime import (no longer needed after query fix).
 */
@Repository
public interface CaisseRepository extends JpaRepository<Caisse, Long> {

    /**
     * FIX 1 — was: findByAgentAndDateOuvertureAndStatut querying on FUNCTION('DATE', c.dateOuverture).
     * Now queries on c.dateCaisse (a dedicated LocalDate column) which is cleaner and index-friendly.
     * Called from CaisseServiceImpl.getCaisseOuverte().
     */
    @Query("SELECT c FROM Caisse c WHERE c.agent = :agent AND c.dateCaisse = :date AND c.statut = :statut")
    Optional<Caisse> findByAgentAndDateCaisseAndStatut(
            @Param("agent") User agent,
            @Param("date") LocalDate date,
            @Param("statut") StatutCaisse statut
    );

    /**
     * FIX 2 — was: findByAgenceIdAndDateOuverture querying on FUNCTION('DATE', c.dateOuverture).
     * Now queries on c.dateCaisse. Called from CaisseServiceImpl.getCaissesAgence().
     */
    @Query("SELECT c FROM Caisse c WHERE c.agence.id = :agenceId AND c.dateCaisse = :date")
    List<Caisse> findByAgenceIdAndDate(
            @Param("agenceId") Long agenceId,
            @Param("date") LocalDate date
    );

    /** History of all cash registers for an agent, most recent first. */
    List<Caisse> findByAgentOrderByDateOuvertureDesc(User agent);

    /** Find a cash register by agent and status (no date filter). */
    Optional<Caisse> findByAgentAndStatut(User agent, StatutCaisse statut);
}