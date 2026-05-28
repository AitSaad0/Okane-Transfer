package com.okane.controller;

import com.okane.entity.User;
import com.okane.dto.NotificationPreferenceDto;
import com.okane.repository.UserRepository;
import com.okane.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        return ResponseEntity.ok(
                notificationService.getUserNotifications(currentUser)
        );
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/clients/notifications/prefs")
    public ResponseEntity<?> updatePrefs(
            HttpServletRequest request,
            @RequestBody NotificationPreferenceDto dto
    ) {
        User user = getCurrentUser(request);
        notificationService.updatePreferences(user, dto);
        return ResponseEntity.ok().build();
    }

    //  replace with @AuthenticationPrincipal once Spring Security is configured
    private User getCurrentUser(HttpServletRequest request) {
        String email = request.getHeader("X-User-Email");
        if (email == null) {
            throw new RuntimeException("Missing X-User-Email header");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}