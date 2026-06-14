package com.okane.repository;

import com.okane.entity.Transfert;
import com.okane.entity.User;
import com.okane.entity.enums.StatutTransfert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransfertRepository extends JpaRepository<Transfert, Long> {

    boolean existsByCodeRetrait(String codeRetrait);

    // Recherche par période
    @Query("""
        SELECT t
        FROM Transfert t
        JOIN FETCH t.corridor c
        JOIN FETCH c.paysOrigine po
        JOIN FETCH c.paysDestination pd
        WHERE t.dateCreation BETWEEN :start AND :end
    """)
    List<Transfert> findByPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Compter les transferts suspects
    @Query("SELECT COUNT(t) FROM Transfert t WHERE t.estSuspect = true")
    long countByEstSuspectTrue();

    // Recherche par code retrait (avec fetch)
    @Query("SELECT t FROM Transfert t JOIN FETCH t.expediteur JOIN FETCH t.corridor WHERE t.codeRetrait = :codeRetrait")
    Optional<Transfert> findByCodeRetraitWithFetch(@Param("codeRetrait") String codeRetrait);

    // Recherche simple par code retrait
    Optional<Transfert> findByCodeRetrait(String codeRetrait);

    // Recherche par téléphone du bénéficiaire
    @Query("SELECT t FROM Transfert t JOIN FETCH t.beneficiaire b JOIN FETCH t.expediteur WHERE b.telephone = :telephone")
    List<Transfert> findByBeneficiaireTelephone(@Param("telephone") String telephone);

    // --- Recherches par téléphone/CIN avec clientId (côté mobile) ---

    @Query("SELECT t FROM Transfert t JOIN FETCH t.expediteur e JOIN FETCH t.corridor JOIN FETCH t.beneficiaire WHERE e.telephone = :telephone AND t.expediteur.id = :clientId")
    List<Transfert> findByExpediteurTelephoneAndClientId(@Param("telephone") String telephone, @Param("clientId") Long clientId);

    @Query("SELECT t FROM Transfert t JOIN FETCH t.beneficiaire b JOIN FETCH t.corridor WHERE b.telephone = :telephone AND t.expediteur.id = :clientId")
    List<Transfert> findByBeneficiaireTelephoneAndClientId(@Param("telephone") String telephone, @Param("clientId") Long clientId);

    @Query("SELECT t FROM Transfert t JOIN FETCH t.expediteur e JOIN FETCH t.corridor JOIN FETCH t.beneficiaire WHERE e.numPieceIdentite = :cin AND t.expediteur.id = :clientId")
    List<Transfert> findByExpediteurCINAndClientId(@Param("cin") String cin, @Param("clientId") Long clientId);

    @Query("SELECT t FROM Transfert t JOIN FETCH t.beneficiaire b JOIN FETCH t.corridor WHERE b.numPieceIdentite = :cin AND t.expediteur.id = :clientId")
    List<Transfert> findByBeneficiaireCINAndClientId(@Param("cin") String cin, @Param("clientId") Long clientId);

    @Query("SELECT t FROM Transfert t JOIN FETCH t.expediteur e JOIN FETCH t.corridor JOIN FETCH t.beneficiaire WHERE t.expediteur.id = :clientId ORDER BY t.dateCreation DESC")
    List<Transfert> findByExpediteurIdOrderByDateCreationDesc(@Param("clientId") Long clientId);

    @Query("SELECT t FROM Transfert t JOIN FETCH t.expediteur e JOIN FETCH t.corridor JOIN FETCH t.beneficiaire WHERE t.codeRetrait = :code AND (t.expediteur.id = :clientId OR t.beneficiaire.id = :clientId)")
    Optional<Transfert> findByCodeRetraitAndClientId(@Param("code") String code, @Param("clientId") Long clientId);

    @Query("SELECT t FROM Transfert t JOIN FETCH t.expediteur e JOIN FETCH t.corridor JOIN FETCH t.beneficiaire WHERE t.beneficiaire.id = :clientId ORDER BY t.dateCreation DESC")
    List<Transfert> findByBeneficiaireIdOrderByDateCreationDesc(@Param("clientId") Long clientId);

    // --- Côté CLIENT : transferts paginés ---
    @Query("""
        SELECT t FROM Transfert t
        WHERE t.expediteur = :client OR t.beneficiaire = :client
        ORDER BY t.dateCreation DESC
    """)
    Page<Transfert> findByClient(@Param("client") com.okane.entity.Client client, Pageable pageable);

    // --- Côté ADMIN : tous les transferts avec filtres ---
    @Query("""
        SELECT t FROM Transfert t
        WHERE (:statut IS NULL OR t.statut = :statut)
          AND (:agenceId IS NULL OR t.agenceEnvoi.id = :agenceId OR t.agencePaiement.id = :agenceId)
          AND (:corridorId IS NULL OR t.corridor.id = :corridorId)
          AND (:debut IS NULL OR t.dateCreation >= :debut)
          AND (:fin IS NULL OR t.dateCreation <= :fin)
        ORDER BY t.dateCreation DESC
    """)
    Page<Transfert> findAllWithFilters(
            @Param("statut") StatutTransfert statut,
            @Param("agenceId") Long agenceId,
            @Param("corridorId") Long corridorId,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin,
            Pageable pageable
    );

    // --- Côté MANAGER : transferts de son agence uniquement ---
    @Query("""
        SELECT t FROM Transfert t
        WHERE (t.agenceEnvoi.id = :agenceId OR t.agencePaiement.id = :agenceId)
          AND (:statut IS NULL OR t.statut = :statut)
        ORDER BY t.dateCreation DESC
    """)
    Page<Transfert> findByAgence(
            @Param("agenceId") Long agenceId,
            @Param("statut") StatutTransfert statut,
            Pageable pageable
    );

    // --- Méthodes pour le dashboard Manager ---

    @Query("SELECT COALESCE(SUM(t.montantEnvoye), 0) FROM Transfert t WHERE t.agenceEnvoi.id = :agenceId AND t.dateCreation >= :debut AND t.statut = 'PAYE'")
    BigDecimal sumVolumeEnvoiJour(@Param("agenceId") Long agenceId, @Param("debut") LocalDateTime debut);

    @Query("SELECT COALESCE(SUM(t.frais), 0) FROM Transfert t WHERE t.agenceEnvoi.id = :agenceId AND t.statut = 'PAYE'")
    BigDecimal sumCommissionsGenerees(@Param("agenceId") Long agenceId);

    @Query("SELECT COUNT(t) FROM Transfert t WHERE t.agenceEnvoi.id = :agenceId AND t.dateCreation >= :debut")
    Long countTransfertsJour(@Param("agenceId") Long agenceId, @Param("debut") LocalDateTime debut);

    @Query("SELECT COUNT(t) FROM Transfert t WHERE t.agenceEnvoi.id = :agenceId AND t.statut = :statut")
    Long countByStatutAndAgence(@Param("agenceId") Long agenceId, @Param("statut") StatutTransfert statut);

    @Query("""
        SELECT u.id, u.nom, u.prenom, COUNT(t), COALESCE(SUM(t.montantEnvoye), 0), COALESCE(SUM(t.frais), 0)
        FROM Transfert t
        JOIN t.agentEnvoi u
        WHERE t.agenceEnvoi.id = :agenceId
          AND t.dateCreation >= :debut
          AND t.statut = 'PAYE'
        GROUP BY u.id, u.nom, u.prenom
        ORDER BY SUM(t.montantEnvoye) DESC
    """)
    List<Object[]> findTopAgentsByVolume(@Param("agenceId") Long agenceId, @Param("debut") LocalDateTime debut);
}