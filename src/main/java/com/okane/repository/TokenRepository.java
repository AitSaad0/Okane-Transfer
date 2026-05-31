package com.okane.repository;

import com.okane.entity.Token;
import com.okane.entity.enums.TypeToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    void deleteByUtilisateurIdAndType(Long userId, TypeToken typeToken);
    Optional<Token> findTokenByValeur(String valeur);
    Optional<Token> findByValeurAndUtilisateurId(String valeur, Long utilisateurId);

}
