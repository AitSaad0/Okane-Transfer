package com.okane.dto.chatbot;

import java.util.List;

public record AiResponse(
        String texte,
        boolean escalated,
        int tokens,
        List<String> quickReplies,
        List<ToolCall> toolCalls
) {}
