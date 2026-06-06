// WatchlistCheckRequest.java
package com.okane.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WatchlistCheckRequest {
    @NotBlank private String fullName;
    private String idNumber;
}