package com.okane.service.chatbot.impl;

import com.okane.dto.chatbot.*;
import com.okane.dto.converter.chatbot.ChatbotConverter;
import com.okane.entity.Client;
import com.okane.entity.chatbot.ChatbotMessage;
import com.okane.entity.chatbot.ChatbotSession;
import com.okane.entity.chatbot.MessageRole;
import com.okane.entity.chatbot.SessionStatus;
import com.okane.repository.ChatbotMessageRepository;
import com.okane.repository.ChatbotSessionRepository;
import com.okane.repository.ClientRepository;
import com.okane.service.chatbot.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatbotSessionRepository sessionRepository;
    private final ChatbotMessageRepository messageRepository;
    private final ClientRepository clientRepository;
    private final ChatbotConverter converter;
    private final PromptBuilder promptBuilder;
    private final AiClientService aiClientService;
    private final ToolRegistry toolRegistry;
    private final ResponseParser responseParser;
    private final EscaladeService escaladeService;

    public ChatbotServiceImpl(ChatbotSessionRepository sessionRepository,
                                ChatbotMessageRepository messageRepository,
                                ClientRepository clientRepository,
                                ChatbotConverter converter,
                                PromptBuilder promptBuilder,
                                AiClientService aiClientService,
                                ToolRegistry toolRegistry,
                                ResponseParser responseParser,
                                EscaladeService escaladeService) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.clientRepository = clientRepository;
        this.converter = converter;
        this.promptBuilder = promptBuilder;
        this.aiClientService = aiClientService;
        this.toolRegistry = toolRegistry;
        this.responseParser = responseParser;
        this.escaladeService = escaladeService;
    }

    @Override
    public ChatbotResponseDTO processMessage(ChatbotRequestDTO request, Long clientId) {
        ChatbotSession session;
        if (request.getSessionId() == null) {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client introuvable"));
            String title = request.getMessage();
            if (title.length() > 80) title = title.substring(0, 80);
            session = ChatbotSession.builder()
                    .client(client)
                    .title(title)
                    .status(SessionStatus.ACTIVE)
                    .messageCount(0)
                    .build();
            session = sessionRepository.save(session);
        } else {
            session = sessionRepository.findByIdAndClientId(request.getSessionId(), clientId)
                    .orElseThrow(() -> new RuntimeException("Session introuvable"));
        }

        ChatbotMessage userMsg = ChatbotMessage.builder()
                .session(session)
                .role(MessageRole.USER)
                .content(request.getMessage())
                .build();
        userMsg = messageRepository.save(userMsg);

        List<ChatbotMessage> historique = new ArrayList<>();
        String contexte = promptBuilder.builderContexte(clientId, request.getMessage(), historique);

        List<ToolDefinition> outils = toolRegistry.getDefinitions();
        List<CallResult> results = new ArrayList<>();
        AiResponse aiResponse;

        int maxIterations = 5;
        int iteration = 0;

        do {
            aiResponse = aiClientService.envoyerMessageAvecOutils(
                    request.getMessage(), contexte, "fr", outils, results);

            if (aiResponse.toolCalls() != null && !aiResponse.toolCalls().isEmpty()) {
                for (ToolCall tc : aiResponse.toolCalls()) {
                    CallResult cr = toolRegistry.executer(tc, clientId);
                    results.add(cr);
                }
            }
            iteration++;
        } while (aiResponse.toolCalls() != null && !aiResponse.toolCalls().isEmpty() && iteration < maxIterations);

        ParserResult parsed = responseParser.parser(aiResponse.texte() != null ? aiResponse.texte() : "");

        boolean escalated = parsed.escalated();
        MessageRole role = escalated ? MessageRole.ESCALATED : MessageRole.BOT;
        String content = escalated
                ? "Votre demande a été transmise à un agent humain. Un agent vous contactera sous peu. **En attente de la réponse de l'agent humain.**"
                : parsed.texte();
        ChatbotMessage botMsg = ChatbotMessage.builder()
                .session(session)
                .role(role)
                .content(content)
                .build();
        botMsg = messageRepository.save(botMsg);

        if (escalated) {
            escaladeService.notifierAgents(session, historique.toString());
            escaladeService.marquerEnAttente(session.getId());
        }

        session.setMessageCount(session.getMessageCount() != null ? session.getMessageCount() + 2 : 2);
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);

        return ChatbotResponseDTO.builder()
                .sessionId(session.getId())
                .sessionTitle(session.getTitle())
                .message(converter.toMessageDTO(botMsg))
                .quickReplies(parsed.quickReplies())
                .escalated(parsed.escalated())
                .build();
    }

    @Override
    public Page<ChatbotMessageDTO> getSessionMessages(Long sessionId, Long clientId, int page, int size) {
        sessionRepository.findByIdAndClientId(sessionId, clientId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId, pageable)
                .map(converter::toMessageDTO);
    }

    @Override
    public List<ChatbotSessionDTO> getSessions(Long clientId, String statusFilter) {
        List<ChatbotSession> sessions;
        if (statusFilter != null && !statusFilter.isBlank()) {
            SessionStatus status = SessionStatus.valueOf(statusFilter.toUpperCase());
            sessions = sessionRepository.findByClientIdAndStatusOrderByLastMessage(clientId, status);
        } else {
            sessions = sessionRepository.findByClientIdOrderByLastMessage(clientId);
        }
        return sessions.stream().map(converter::toSessionDTO).toList();
    }

    @Override
    public ChatbotSessionDTO createSession(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        ChatbotSession session = ChatbotSession.builder()
                .client(client)
                .title("Nouvelle conversation")
                .status(SessionStatus.ACTIVE)
                .messageCount(0)
                .build();
        session = sessionRepository.save(session);
        return converter.toSessionDTO(session);
    }

    @Override
    public ChatbotSessionDTO updateTitle(Long sessionId, Long clientId, String title) {
        ChatbotSession session = sessionRepository.findByIdAndClientId(sessionId, clientId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));
        session.setTitle(title);
        session = sessionRepository.save(session);
        return converter.toSessionDTO(session);
    }

    @Override
    public void deleteSession(Long sessionId, Long clientId) {
        ChatbotSession session = sessionRepository.findByIdAndClientId(sessionId, clientId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));
        sessionRepository.delete(session);
    }

    @Override
    public ChatbotSessionDTO escalateSession(Long sessionId, Long clientId) {
        ChatbotSession session = sessionRepository.findByIdAndClientId(sessionId, clientId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));
        escaladeService.marquerEnAttente(session.getId());
        escaladeService.notifierAgents(session, "Escalade manuelle par le client via le bouton");
        ChatbotMessage msg = ChatbotMessage.builder()
                .session(session)
                .role(MessageRole.ESCALATED)
                .content("Votre demande a été transmise à un agent humain. Un agent vous contactera sous peu.")
                .build();
        messageRepository.save(msg);
        session.setUpdatedAt(LocalDateTime.now());
        session = sessionRepository.save(session);
        return converter.toSessionDTO(session);
    }

    @Override
    public ChatbotSessionDTO archiveSession(Long sessionId, Long clientId) {
        ChatbotSession session = sessionRepository.findByIdAndClientId(sessionId, clientId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));
        session.setStatus(SessionStatus.ARCHIVED);
        session = sessionRepository.save(session);
        return converter.toSessionDTO(session);
    }

    @Override
    public ChatbotSessionDTO restoreSession(Long sessionId, Long clientId) {
        ChatbotSession session = sessionRepository.findByIdAndClientId(sessionId, clientId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));
        session.setStatus(SessionStatus.ACTIVE);
        session = sessionRepository.save(session);
        return converter.toSessionDTO(session);
    }
}
