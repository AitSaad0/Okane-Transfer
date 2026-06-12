package com.okane.service.chatbot.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.okane.dto.chatbot.AiResponse;
import com.okane.dto.chatbot.CallResult;
import com.okane.dto.chatbot.ToolCall;
import com.okane.dto.chatbot.ToolDefinition;
import com.okane.service.chatbot.AiClientService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Service
public class AiClientServiceImpl implements AiClientService {

    private static final Logger log = LoggerFactory.getLogger(AiClientServiceImpl.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String systemPrompt;

    public AiClientServiceImpl() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        try {
            var resource = new ClassPathResource("prompts/chatbot-system.txt");
            systemPrompt = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            systemPrompt = "Tu es un assistant clientèle pour Okane Transfer.";
        }
    }

    @Override
    public AiResponse envoyerMessage(String message, String historiqueContext, String langue) {
        return envoyerMessageAvecOutils(message, historiqueContext, langue, List.of(), List.of());
    }

    private static final int MAX_429_RETRIES = 5;

    @Override
    public AiResponse envoyerMessageAvecOutils(String message, String historiqueContext, String langue,
                                                List<ToolDefinition> outils, List<CallResult> resultatsAppelsPrecedents) {
        return envoyerMessageAvecOutils(message, historiqueContext, langue, outils, resultatsAppelsPrecedents, 0);
    }

    private AiResponse envoyerMessageAvecOutils(String message, String historiqueContext, String langue,
                                                List<ToolDefinition> outils, List<CallResult> resultatsAppelsPrecedents,
                                                int retry429Count) {
        try {
            String apiKey = env("AI_API_KEY", "");
            String model = env("AI_MODEL", "gpt-4o-mini");
            String apiUrl = env("AI_API_URL", "https://api.openai.com/v1/chat/completions");

            if (apiKey.isEmpty()) {
                return fallbackResponse("Service IA non configuré. Veuillez contacter un agent.");
            }

            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            body.put("temperature", 0.7);

            ArrayNode messages = body.putArray("messages");
            messages.addObject().put("role", "system").put("content", systemPrompt + "\n\n" + historiqueContext);
            messages.addObject().put("role", "user").put("content", message);

            if (!resultatsAppelsPrecedents.isEmpty()) {
                ObjectNode assistantMsg = messages.addObject();
                assistantMsg.put("role", "assistant");
                assistantMsg.putNull("content");
                ArrayNode toolCallsArray = assistantMsg.putArray("tool_calls");
                for (CallResult cr : resultatsAppelsPrecedents) {
                    ObjectNode tc = toolCallsArray.addObject();
                    tc.put("id", cr.toolCallId());
                    tc.put("type", "function");
                    ObjectNode func = tc.putObject("function");
                    func.put("name", cr.functionName());
                    func.put("arguments", cr.arguments());
                }
                for (CallResult cr : resultatsAppelsPrecedents) {
                    messages.addObject().put("role", "tool")
                            .put("tool_call_id", cr.toolCallId())
                            .put("content", cr.result());
                }
            }

            if (!outils.isEmpty()) {
                ArrayNode toolsArray = body.putArray("tools");
                for (ToolDefinition def : outils) {
                    ObjectNode tool = toolsArray.addObject();
                    tool.put("type", "function");
                    ObjectNode func = tool.putObject("function");
                    func.put("name", def.name());
                    func.put("description", def.description());
                    func.set("parameters", objectMapper.valueToTree(def.parameters()));
                }
            }

            String requestBody = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                if (retry429Count < MAX_429_RETRIES) {
                    Thread.sleep(2000);
                    return envoyerMessageAvecOutils(message, historiqueContext, langue, outils, resultatsAppelsPrecedents, retry429Count + 1);
                }
                return fallbackResponse("Le service IA est temporairement saturé. Veuillez réessayer plus tard ou contacter un agent.");
            }

            if (response.statusCode() != 200) {
                log.error("AI API error ({}): {}", response.statusCode(), response.body());
                return fallbackResponse("Le service IA est temporairement indisponible. Veuillez réessayer plus tard ou contacter un agent.");
            }

            return parseResponse(response.body());

        } catch (InterruptedException e) {
            log.error("AI request interrupted", e);
            Thread.currentThread().interrupt();
            return fallbackResponse("La requête a été interrompue. Veuillez réessayer.");
        } catch (Exception e) {
            log.error("AI request failed: {}", e.getMessage(), e);
            return fallbackResponse("Une erreur est survenue. Veuillez réessayer.");
        }
    }

    private AiResponse parseResponse(String responseBody) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode message = root.path("choices").get(0).path("message");

        String content = message.path("content").asText(null);
        JsonNode toolCallsNode = message.path("tool_calls");

        if (toolCallsNode != null && toolCallsNode.isArray() && toolCallsNode.size() > 0) {
            List<ToolCall> toolCalls = new ArrayList<>();
            for (JsonNode tc : toolCallsNode) {
                toolCalls.add(new ToolCall(
                        tc.path("id").asText(),
                        tc.path("function").path("name").asText(),
                        tc.path("function").path("arguments").asText()
                ));
            }
            return new AiResponse(null, false, 0, List.of(), toolCalls);
        }

        String texte = content != null ? content : "";
        boolean escalated = false;

        List<String> quickReplies = new ArrayList<>();
        int idx;
        while ((idx = texte.indexOf("[Suggestion:")) != -1) {
            int end = texte.indexOf("]", idx);
            if (end != -1) {
                quickReplies.add(texte.substring(idx + 12, end).trim());
                texte = texte.substring(0, idx) + texte.substring(end + 1);
            } else {
                break;
            }
        }

        int tokens = root.path("usage").path("total_tokens").asInt(0);
        return new AiResponse(texte.trim(), escalated, tokens, quickReplies, List.of());
    }

    private AiResponse fallbackResponse(String message) {
        return new AiResponse(message, false, 0, List.of(), List.of());
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null && !value.isBlank() ? value : defaultValue;
    }
}
