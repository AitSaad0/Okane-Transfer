package com.okane.controller.chatbot;

import com.okane.dto.chatbot.*;
import com.okane.entity.Client;
import com.okane.entity.Pays;
import com.okane.entity.User;
import com.okane.repository.ClientRepository;
import com.okane.repository.PaysRepository;
import com.okane.repository.UserRepository;
import com.okane.service.chatbot.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PaysRepository paysRepository;

    public ChatbotController(ChatbotService chatbotService,
                              ClientRepository clientRepository,
                              UserRepository userRepository,
                              PaysRepository paysRepository) {
        this.chatbotService = chatbotService;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.paysRepository = paysRepository;
    }

    @PostMapping("/message")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ChatbotResponseDTO> envoyerMessage(
            @Valid @RequestBody ChatbotRequestDTO request,
            Authentication authentication) {
        Long clientId = resolveClientId(authentication);
        return ResponseEntity.ok(chatbotService.processMessage(request, clientId));
    }

    @GetMapping("/sessions")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<ChatbotSessionDTO>> getSessions(
            @RequestParam(required = false) String status,
            Authentication authentication) {
        Long clientId = resolveClientId(authentication);
        return ResponseEntity.ok(chatbotService.getSessions(clientId, status));
    }

    @PostMapping("/sessions")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ChatbotSessionDTO> createSession(Authentication authentication) {
        Long clientId = resolveClientId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(chatbotService.createSession(clientId));
    }

    @GetMapping("/sessions/{id}/messages")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<ChatbotMessageDTO>> getSessionMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        Long clientId = resolveClientId(authentication);
        return ResponseEntity.ok(chatbotService.getSessionMessages(id, clientId, page, size));
    }

    @PatchMapping("/sessions/{id}/title")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ChatbotSessionDTO> updateTitle(
            @PathVariable Long id,
            @Valid @RequestBody ChatbotSessionTitleDTO request,
            Authentication authentication) {
        Long clientId = resolveClientId(authentication);
        return ResponseEntity.ok(chatbotService.updateTitle(id, clientId, request.getTitle()));
    }

    @DeleteMapping("/sessions/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id, Authentication authentication) {
        Long clientId = resolveClientId(authentication);
        chatbotService.deleteSession(id, clientId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/sessions/{id}/archive")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ChatbotSessionDTO> archiveSession(@PathVariable Long id, Authentication authentication) {
        Long clientId = resolveClientId(authentication);
        return ResponseEntity.ok(chatbotService.archiveSession(id, clientId));
    }

    @PatchMapping("/sessions/{id}/restore")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ChatbotSessionDTO> restoreSession(@PathVariable Long id, Authentication authentication) {
        Long clientId = resolveClientId(authentication);
        return ResponseEntity.ok(chatbotService.restoreSession(id, clientId));
    }

    @PostMapping("/sessions/{id}/escalate")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ChatbotSessionDTO> escalateSession(@PathVariable Long id, Authentication authentication) {
        Long clientId = resolveClientId(authentication);
        return ResponseEntity.ok(chatbotService.escalateSession(id, clientId));
    }

    private Long resolveClientId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return clientRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Pays fallback = paysRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new RuntimeException("Aucun pays trouvé"));
                    Client nouveau = Client.builder()
                            .nom(user.getNom())
                            .prenom(user.getPrenom())
                            .email(user.getEmail())
                            .telephone(user.getTelephone() != null ? user.getTelephone() : "")
                            .numPieceIdentite("PENDING_" + user.getId())
                            .pays(fallback)
                            .user(user)
                            .estSurListeSurveillance(false)
                            .deleted(false)
                            .build();
                    return clientRepository.save(nouveau);
                }).getId();
    }
}
