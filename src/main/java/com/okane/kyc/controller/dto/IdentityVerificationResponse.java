package com.okane.kyc.controller.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class IdentityVerificationResponse {
    private boolean valid;
    private String documentNumber;
    private String countryCode;
    private String documentType;
    private String message;
    private boolean watchlistMatch;
    private String alertId; // non-null if alert was generated
}