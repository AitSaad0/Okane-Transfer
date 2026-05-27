package com.okane.reports.repository;

import com.okane.clients_transfers.bean.Transfert;
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
    public long countByEstSuspectTrue() {

        return entityManager.createQuery("""
            SELECT COUNT(t)
            FROM Transfert t
            WHERE t.estSuspect = true
        """, Long.class).getSingleResult();
    }

    public Transfert save(Transfert transfert) {

        if (transfert.getId() == null) {
            entityManager.persist(transfert);
            return transfert;
        }

        return entityManager.merge(transfert);
    }
    public long count() {

        return entityManager.createQuery("""
            SELECT COUNT(t)
            FROM Transfert t
        """, Long.class).getSingleResult();
    }


}