package com.okane.repository;

import com.okane.entity.KycAlert;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class KycAlertRepository {

    @PersistenceContext
    private EntityManager em;

    public KycAlert save(KycAlert alert) {
        if (alert.getId() == null) {
            em.persist(alert);
            return alert;
        }
        return em.merge(alert);
    }

    public Optional<KycAlert> findById(UUID id) {
        return Optional.ofNullable(em.find(KycAlert.class, id));
    }

    public List<KycAlert> findAll() {
        return em.createQuery("SELECT a FROM KycAlert a ORDER BY a.createdAt DESC",
                KycAlert.class).getResultList();
    }
}