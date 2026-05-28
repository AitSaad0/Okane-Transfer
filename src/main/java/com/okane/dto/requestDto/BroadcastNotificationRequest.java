package com.okane.dto.requestDto;


import com.okane.entity.enums.CanalNotification;
import com.okane.entity.enums.TypeNotification;
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