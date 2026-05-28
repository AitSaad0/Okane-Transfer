package com.okane.clients_transfers.repository;

import com.okane.clients_transfers.bean.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByUserId(Long userId);

    boolean existsByNumPieceIdentite(String numPieceIdentite);

    boolean existsByTelephone(String telephone);
}