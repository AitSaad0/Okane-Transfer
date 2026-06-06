package com.okane.dto.responseDto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClientActivityResponseDto {
        private String        action;
        private String        details;
        private String        ipAddress;
        private LocalDateTime timestamp;
        private String        type;

}
