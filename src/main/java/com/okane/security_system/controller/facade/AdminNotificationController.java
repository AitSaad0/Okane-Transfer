package com.okane.security_system.controller.facade;


import com.okane.security_system.controller.dto.BroadcastNotificationRequest;
import com.okane.security_system.service.facade.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
