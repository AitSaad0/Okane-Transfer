package com.okane.dto.chatbot;

public record CallResult(
        String toolCallId,
        String functionName,
        String arguments,
        String result
) {}
