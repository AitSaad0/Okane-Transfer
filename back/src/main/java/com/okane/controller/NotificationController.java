package com.okane.controller;

import com.okane.dto.NotificationPreferenceDto;
import com.okane.dto.requestDto.BroadcastNotificationRequest;
import com.okane.dto.responseDto.NotificationResponseDto;
import com.okane.entity.User;
import com.okane.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(
                notificationService.getUserNotifications(currentUser)
        );
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/notifications/preferences")
    public ResponseEntity<Void> updatePrefs(
            @AuthenticationPrincipal User currentUser,
            @RequestBody NotificationPreferenceDto dto
    ) {
        notificationService.updatePreferences(currentUser, dto);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/notifications/preferences")
    public ResponseEntity<NotificationPreferenceDto> getPreferences(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(
                NotificationPreferenceDto.builder()
                        .emailActive(currentUser.getNotificationEmail())
                        .smsActive(currentUser.getNotificationSms())
                        .pushActive(currentUser.getNotificationPush())
                        .build()
        );
    }

}