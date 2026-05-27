package com.okane.security_system.controller.dto;

import com.okane.shared.CanalNotification;
import com.okane.shared.TypeNotification;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {

    private Long id;

    private TypeNotification type;

    private CanalNotification canal;

    private String contenu;

    private Boolean lu;

    private LocalDateTime dateEnvoi;
}
