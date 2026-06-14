package com.okane.service.chatbot;

import com.okane.entity.chatbot.ChatbotSession;

public interface EscaladeService {
    void notifierAgents(ChatbotSession session, String contexte);
    void marquerEnAttente(Long sessionId);
}
