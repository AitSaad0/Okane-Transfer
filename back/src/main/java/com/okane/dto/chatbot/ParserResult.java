package com.okane.dto.chatbot;

import java.util.List;

public record ParserResult(
        String texte,
        boolean escalated,
        List<String> quickReplies,
        int tokens
) {}
