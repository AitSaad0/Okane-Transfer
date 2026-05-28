package com.okane.kyc.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class WatchlistEntryResponse {
    private UUID id;
    private String fullName;
    private String idNumber;
    private String source;
    private String reason;
    private LocalDateTime addedAt;
    private String addedBy;
}
