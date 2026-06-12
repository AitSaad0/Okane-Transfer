package com.okane.service.chatbot.impl;

import com.okane.entity.chatbot.ChatbotSession;
import com.okane.entity.chatbot.SessionStatus;
import com.okane.repository.ChatbotSessionRepository;
import com.okane.service.chatbot.EscaladeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class EscaladeServiceImpl implements EscaladeService {

    private static final Logger log = LoggerFactory.getLogger(EscaladeServiceImpl.class);

    private final ChatbotSessionRepository sessionRepository;

    public EscaladeServiceImpl(ChatbotSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void notifierAgents(ChatbotSession session, String contexte) {
        log.info("=== ESCALADE REQUISE ===");
        log.info("Session ID: {}", session.getId());
        log.info("Client ID: {}", session.getClient().getId());
        log.info("Contexte: {}", contexte);
        log.info("=== FIN ESCALADE ===");
    }

    @Override
    public void marquerEnAttente(Long sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setStatus(SessionStatus.WAITING_AGENT);
            session.setUpdatedAt(LocalDateTime.now());
            sessionRepository.save(session);
            log.info("Session {} marquée en attente d'agent", sessionId);
        });
    }
}
