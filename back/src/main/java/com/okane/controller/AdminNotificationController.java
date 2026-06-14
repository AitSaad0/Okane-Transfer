package com.okane.controller;

import com.okane.dto.requestDto.BroadcastNotificationRequest;
import com.okane.dto.responseDto.BroadcastNotificationResponse;
import com.okane.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcast(
            @RequestBody BroadcastNotificationRequest request
    ) {
        notificationService.broadcast(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/broadcast")
    public ResponseEntity<List<BroadcastNotificationResponse>> getAllBroadcasts() {
        return ResponseEntity.ok(notificationService.getAllBroadcasts());
    }
}