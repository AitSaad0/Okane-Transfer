package com.okane.repository;

import com.okane.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

        Optional<Client> findByUserId(Long userId);

        Optional<Client> findByNumPieceIdentite(String numPieceIdentite);

        boolean existsByNumPieceIdentite(String numPieceIdentite);

        boolean existsByTelephone(String telephone);

}
