package com.okane.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WatchlistEntryRequest {
    @NotBlank private String fullName;
    private String idNumber;
    @NotBlank private String source;
    private String reason;
}