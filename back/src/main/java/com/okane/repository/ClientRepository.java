package com.okane.repository;

import com.okane.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

        Optional<Client> findByUserId(Long userId);

        Optional<Client> findByNumPieceIdentite(String numPieceIdentite);

        boolean existsByNumPieceIdentite(String numPieceIdentite);

        boolean existsByTelephone(String telephone);
    @Query("""
        SELECT c FROM Client c
        WHERE c.deleted = false
        AND (
            LOWER(c.nom)              LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(c.prenom)           LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(c.email)            LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(c.numPieceIdentite) LIKE LOWER(CONCAT('%', :q, '%'))
        )
    """)
    List<Client> searchClients(@Param("q") String query);

    Optional<Client> findByEmail(String email);

    Optional<Client> findByUser(com.okane.entity.User user);

    Optional<Client> findByTelephone(String telephone);

}
