package com.okane.dto.converter.chatbot;

import com.okane.dto.chatbot.ChatbotMessageDTO;
import com.okane.dto.chatbot.ChatbotSessionDTO;
import com.okane.entity.chatbot.ChatbotMessage;
import com.okane.entity.chatbot.ChatbotSession;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ChatbotConverter {

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private String fmt(LocalDateTime dt) {
        return dt != null ? dt.format(ISO_FMT) : null;
    }

    public ChatbotSessionDTO toSessionDTO(ChatbotSession session) {
        return ChatbotSessionDTO.builder()
                .id(session.getId())
                .title(session.getTitle())
                .status(session.getStatus())
                .createdAt(fmt(session.getCreatedAt()))
                .updatedAt(fmt(session.getUpdatedAt()))
                .messageCount(session.getMessageCount())
                .unreadCount(0)
                .build();
    }

    public ChatbotMessageDTO toMessageDTO(ChatbotMessage message) {
        return ChatbotMessageDTO.builder()
                .id(message.getId())
                .sessionId(message.getSession().getId())
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(fmt(message.getCreatedAt()))
                .build();
    }
}
