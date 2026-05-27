package com.okane.security_system.controller.facade;

import com.okane.network_users.bean.User;
import com.okane.security_system.controller.dto.NotificationPreferenceDto;
import com.okane.security_system.service.facade.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final com.okane.network_users.repository.UserRepository userRepository;

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications() {

        User currentUser = getCurrentUser();

        return ResponseEntity.ok(
                notificationService.getUserNotifications(currentUser)
        );
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id
    ) {

        notificationService.markAsRead(id);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/clients/notifications/prefs")
    public ResponseEntity<?> updatePrefs(
            @RequestBody NotificationPreferenceDto dto
    ) {

        User currentUser = userRepository.findById(1L).orElseThrow();

        notificationService.updatePreferences(
                currentUser,
                dto
        );

        return ResponseEntity.ok().build();
    }

    private User getCurrentUser() {

        // Spring Security principal
        return null;
    }
}
