package com.okane.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceDto {

    private Boolean emailActive;

    private Boolean pushActive;

    private Boolean smsActive;
}