package com.okane.dto.chatbot;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotRequestDTO {
    private Long sessionId;
    @NotBlank
    private String message;
}
