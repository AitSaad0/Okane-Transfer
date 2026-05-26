package com.okane.geographic_monetary_reference.repository;

import com.okane.geographic_monetary_reference.bean.Devise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviseRepository extends JpaRepository<Devise, Long> {
    boolean existsByCode(String code);
    Optional<Devise> findByCode(String code);
}