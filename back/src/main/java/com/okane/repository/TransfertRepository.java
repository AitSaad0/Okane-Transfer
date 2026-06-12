package com.okane.repository;

import com.okane.entity.Transfert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransfertRepository extends JpaRepository<Transfert, Long> {

    boolean existsByCodeRetrait(String codeRetrait);

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

    @Query("SELECT COUNT(t) FROM Transfert t WHERE t.estSuspect = true")
    long countByEstSuspectTrue();

    @Query("SELECT t FROM Transfert t JOIN FETCH t.expediteur JOIN FETCH t.corridor WHERE t.codeRetrait = :codeRetrait")
    Optional<Transfert> findByCodeRetrait(@Param("codeRetrait") String codeRetrait);

    @Query("SELECT t FROM Transfert t JOIN FETCH t.beneficiaire b JOIN FETCH t.expediteur WHERE b.telephone = :telephone")
    List<Transfert> findByBeneficiaireTelephone(@Param("telephone") String telephone);

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

    // save()  → inherited from JpaRepository
    // count() → inherited from JpaRepository
}