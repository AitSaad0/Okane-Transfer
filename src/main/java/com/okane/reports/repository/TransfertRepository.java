package com.okane.reports.repository;

import com.okane.entity.Transfert;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TransfertRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Transfert> findByPeriod(
            LocalDateTime start,
            LocalDateTime end
    ) {

        TypedQuery<Transfert> query = entityManager.createQuery("""
            SELECT t
            FROM Transfert t
            JOIN FETCH t.corridor c
            JOIN FETCH c.paysOrigine po
            JOIN FETCH c.paysDestination pd
            WHERE t.dateCreation BETWEEN :start AND :end
        """, Transfert.class);

        query.setParameter("start", start);
        query.setParameter("end", end);

        return query.getResultList();
    }
}