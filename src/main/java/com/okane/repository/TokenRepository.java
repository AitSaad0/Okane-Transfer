package com.okane.repository;

import com.okane.entity.Token;
import com.okane.entity.enums.TypeToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {
    void deleteByUtilisateurIdAndType(Long userId, TypeToken typeToken);
}
