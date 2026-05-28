package com.okane.dto.responseDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;
}