package com.okane.repository;

import com.okane.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

        Optional<Client> findByUserId(Long userId);

        boolean existsByNumPieceIdentite(String numPieceIdentite);

        boolean existsByTelephone(String telephone);

}
