package com.okane.dto.chatbot;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotResponseDTO {
    private Long sessionId;
    private String sessionTitle;
    private ChatbotMessageDTO message;
    private List<String> quickReplies;
    private boolean escalated;
}
