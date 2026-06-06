package com.okane.repository;

import com.okane.entity.Corridor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorridorRepository extends JpaRepository<Corridor, Long> {
    List<Corridor> findByActifTrue();
}