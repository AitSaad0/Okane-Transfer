package com.okane.dto.chatbot;

import com.okane.entity.chatbot.SessionStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotSessionDTO {
    private Long id;
    private String title;
    private SessionStatus status;
    private String createdAt;
    private String updatedAt;
    private Integer messageCount;
    private Integer unreadCount;
}
