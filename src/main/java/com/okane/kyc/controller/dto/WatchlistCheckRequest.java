// WatchlistCheckRequest.java
package com.okane.kyc.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WatchlistCheckRequest {
    @NotBlank private String fullName;
    private String idNumber;
}