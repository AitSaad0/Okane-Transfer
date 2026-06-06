package com.okane.dto.responseDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpResponseDTO {
    private String message;
    private boolean success;
}