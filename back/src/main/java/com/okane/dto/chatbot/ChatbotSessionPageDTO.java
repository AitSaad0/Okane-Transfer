package com.okane.dto.chatbot;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotSessionPageDTO {
    private List<ChatbotSessionDTO> sessions;
    private int totalPages;
    private int currentPage;
    private long totalElements;
}
