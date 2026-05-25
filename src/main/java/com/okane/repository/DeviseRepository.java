package com.okane.repository;

import com.okane.entity.Devise;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DeviseRepository {

    private final SessionFactory sessionFactory;

    // Constructor injection with SessionFactory
    public DeviseRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Transactional(readOnly = true)
    public List<Devise> findAll() {
        return getCurrentSession()
                .createQuery("from Devise", Devise.class)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public Optional<Devise> findById(UUID id) {
        Devise devise = getCurrentSession().get(Devise.class, id);
        return Optional.ofNullable(devise);
    }

    @Transactional(readOnly = true)
    public Optional<Devise> findByCode(String code) {
        return getCurrentSession()
                .createQuery("from Devise where code = :code", Devise.class)
                .setParameter("code", code)
                .uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        Long count = getCurrentSession()
                .createQuery("select count(*) from Devise where code = :code", Long.class)
                .setParameter("code", code)
                .uniqueResult();
        return count != null && count > 0;
    }

    @Transactional
    public Devise save(Devise devise) {
        getCurrentSession().persist(devise);
        return devise;
    }

    @Transactional
    public Devise update(Devise devise) {
        return getCurrentSession().merge(devise);
    }

    @Transactional
    public void deleteById(UUID id) {
        Devise devise = getCurrentSession().get(Devise.class, id);
        if (devise != null) {
            getCurrentSession().remove(devise);
        }
    }
}