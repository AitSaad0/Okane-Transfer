package com.okane.dto.responseDto;

import com.okane.entity.enums.CanalNotification;
import com.okane.entity.enums.TypeNotification;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastNotificationResponse {

    private TypeNotification type;
    private CanalNotification canal;
    private String contenu;
    private LocalDateTime dateEnvoi;
}