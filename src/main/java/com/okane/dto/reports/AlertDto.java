package com.okane.dto.reports;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDto {

    private String type;

    private String message;

    private String severity;

    private LocalDateTime timestamp;
}