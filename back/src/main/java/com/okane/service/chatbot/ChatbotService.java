package com.okane.service.chatbot;

import com.okane.dto.chatbot.ChatbotRequestDTO;
import com.okane.dto.chatbot.ChatbotResponseDTO;
import com.okane.dto.chatbot.ChatbotSessionDTO;
import com.okane.dto.chatbot.ChatbotMessageDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ChatbotService {
    ChatbotResponseDTO processMessage(ChatbotRequestDTO request, Long clientId);
    Page<ChatbotMessageDTO> getSessionMessages(Long sessionId, Long clientId, int page, int size);
    List<ChatbotSessionDTO> getSessions(Long clientId, String statusFilter);
    ChatbotSessionDTO createSession(Long clientId);
    ChatbotSessionDTO updateTitle(Long sessionId, Long clientId, String title);
    void deleteSession(Long sessionId, Long clientId);
    ChatbotSessionDTO archiveSession(Long sessionId, Long clientId);
    ChatbotSessionDTO restoreSession(Long sessionId, Long clientId);
    ChatbotSessionDTO escalateSession(Long sessionId, Long clientId);
}
