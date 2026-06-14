package com.okane.repository;

import com.okane.entity.WatchlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WatchlistEntryRepository extends JpaRepository<WatchlistEntry, UUID> {

    Optional<WatchlistEntry> findByIdNumber(String idNumber);
    @Query("SELECT w FROM WatchlistEntry w WHERE LOWER(w.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<WatchlistEntry> findByNameContaining(@Param("name") String name);
}