package com.okane.repository;

import com.okane.entity.Transfert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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

    // save()  → inherited from JpaRepository
    // count() → inherited from JpaRepository
}