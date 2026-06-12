package com.okane.dto.chatbot;

import com.okane.entity.chatbot.MessageRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotMessageDTO {
    private Long id;
    private Long sessionId;
    private MessageRole role;
    private String content;
    private String createdAt;
}
