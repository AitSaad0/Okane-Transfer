package com.okane.kyc.repository;

import com.okane.kyc.bean.WatchlistEntry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WatchlistEntryRepository {

    @PersistenceContext
    private EntityManager em;

    public WatchlistEntry save(WatchlistEntry entry) {
        if (entry.getId() == null) {
            em.persist(entry);
            return entry;
        }
        return em.merge(entry);
    }

    public List<WatchlistEntry> findAll() {
        return em.createQuery("SELECT w FROM WatchlistEntry w ORDER BY w.addedAt DESC",
                WatchlistEntry.class).getResultList();
    }

    public Optional<WatchlistEntry> findByIdNumber(String idNumber) {
        return em.createQuery(
                        "SELECT w FROM WatchlistEntry w WHERE w.idNumber = :id",
                        WatchlistEntry.class)
                .setParameter("id", idNumber)
                .getResultStream().findFirst();
    }

    // Fuzzy name match: case-insensitive contains
    public List<WatchlistEntry> findByNameContaining(String name) {
        return em.createQuery(
                        "SELECT w FROM WatchlistEntry w WHERE LOWER(w.fullName) LIKE :name",
                        WatchlistEntry.class)
                .setParameter("name", "%" + name.toLowerCase() + "%")
                .getResultList();
    }

    public Optional<WatchlistEntry> findById(UUID id) {
        return Optional.ofNullable(em.find(WatchlistEntry.class, id));
    }
}