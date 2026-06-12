package com.okane.dto.chatbot;

public record ToolCall(
        String id,
        String functionName,
        String arguments
) {}
