package com.okane.repository;

import com.okane.entity.chatbot.ChatbotSession;
import com.okane.entity.chatbot.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatbotSessionRepository extends JpaRepository<ChatbotSession, Long> {

    @Query("SELECT s FROM ChatbotSession s LEFT JOIN ChatbotMessage m ON m.session = s " +
           "WHERE s.client.id = :clientId AND s.status = :status " +
           "GROUP BY s.id ORDER BY COALESCE(MAX(m.createdAt), s.updatedAt) DESC")
    List<ChatbotSession> findByClientIdAndStatusOrderByLastMessage(@Param("clientId") Long clientId,
                                                                   @Param("status") SessionStatus status);

    @Query("SELECT s FROM ChatbotSession s LEFT JOIN ChatbotMessage m ON m.session = s " +
           "WHERE s.client.id = :clientId " +
           "GROUP BY s.id ORDER BY COALESCE(MAX(m.createdAt), s.updatedAt) DESC")
    List<ChatbotSession> findByClientIdOrderByLastMessage(@Param("clientId") Long clientId);

    Optional<ChatbotSession> findByIdAndClientId(Long id, Long clientId);
}
