package com.okane.service.chatbot;

import com.okane.dto.chatbot.AiResponse;
import com.okane.dto.chatbot.CallResult;
import com.okane.dto.chatbot.ToolDefinition;

import java.util.List;

public interface AiClientService {
    AiResponse envoyerMessage(String message, String historiqueContext, String langue);
    AiResponse envoyerMessageAvecOutils(String message, String historiqueContext, String langue, List<ToolDefinition> outils, List<CallResult> resultatsAppelsPrecedents);
}
