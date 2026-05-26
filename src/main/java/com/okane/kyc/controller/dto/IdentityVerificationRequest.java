package com.okane.kyc.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IdentityVerificationRequest {
    @NotBlank private String countryCode; // ISO-3 e.g. "FRA", "MAR", "SEN"
    @NotBlank private String documentType; // "CNI", "PASSPORT", "CIN"
    @NotBlank private String documentNumber;
    @NotBlank private String fullName;
}