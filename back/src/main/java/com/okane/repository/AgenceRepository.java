package com.okane.repository;

import com.okane.entity.Agence;
import com.okane.entity.enums.StatutAgence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface AgenceRepository extends JpaRepository<Agence, Long> {

    boolean existsByNomAndAdresse(String nom, String adresse);

    Page<Agence> findByPaysIdAndStatut(Long paysId, StatutAgence statut, Pageable pageable);
    Page<Agence> findByPaysId(Long paysId, Pageable pageable);
    Page<Agence> findByStatut(StatutAgence statut, Pageable pageable);

    // Volume envoi du jour
    @Query("""
        SELECT COALESCE(SUM(t.montantEnvoye), 0)
        FROM Transfert t
        WHERE t.agenceEnvoi.id = :agenceId
          AND t.dateCreation >= :debutJour
          AND t.statut <> com.okane.entity.enums.StatutTransfert.ANNULE
    """)
    BigDecimal sumVolumeEnvoiJour(@Param("agenceId") Long agenceId,
                                  @Param("debutJour") LocalDateTime debutJour);

    // Volume paiement du jour
    @Query("""
        SELECT COALESCE(SUM(t.montantNet), 0)
        FROM Transfert t
        WHERE t.agencePaiement.id = :agenceId
          AND t.datePaiement >= :debutJour
          AND t.statut = com.okane.entity.enums.StatutTransfert.PAYE
    """)
    BigDecimal sumVolumePaiementJour(@Param("agenceId") Long agenceId,
                                     @Param("debutJour") LocalDateTime debutJour);

    // Nombre de transferts du jour (envoi)
    @Query("""
        SELECT COUNT(t)
        FROM Transfert t
        WHERE t.agenceEnvoi.id = :agenceId
          AND t.dateCreation >= :debutJour
    """)
    Long countTransfertsJour(@Param("agenceId") Long agenceId,
                             @Param("debutJour") LocalDateTime debutJour);

    // Comptage par statut
    @Query("""
        SELECT COUNT(t)
        FROM Transfert t
        WHERE t.agenceEnvoi.id = :agenceId
          AND t.statut = :statut
    """)
    Long countByStatut(@Param("agenceId") Long agenceId,
                       @Param("statut") com.okane.entity.enums.StatutTransfert statut);

    // Commissions générées (partAgence sur GRILLE_TARIFAIRE × transferts PAYE)
    @Query("""
        SELECT COALESCE(SUM(g.partAgence), 0)
        FROM Transfert t
        JOIN GrilleTarifaire g ON g.corridor.id = t.corridor.id
        WHERE t.agenceEnvoi.id = :agenceId
          AND t.statut = com.okane.entity.enums.StatutTransfert.PAYE
          AND t.montantEnvoye BETWEEN g.montantMin AND g.montantMax
    """)
    BigDecimal sumCommissionsGenerees(@Param("agenceId") Long agenceId);
}