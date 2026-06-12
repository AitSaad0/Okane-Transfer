package com.okane.repository;

import com.okane.entity.chatbot.ChatbotMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {

    Page<ChatbotMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId, Pageable pageable);

    int countBySessionId(Long sessionId);
}
