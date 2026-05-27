package com.okane.security_system.controller.dto;


import com.okane.shared.CanalNotification;
import com.okane.shared.TypeNotification;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastNotificationRequest {

    private TypeNotification type;

    private CanalNotification canal;

    private String contenu;
}