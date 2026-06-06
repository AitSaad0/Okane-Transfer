package com.okane.dto.responseDto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversionResponseDTO {
    private BigDecimal montantSource;
    private String deviseSource;
    private BigDecimal montantConverti;
    private String deviseDestination;
    private BigDecimal tauxApplique;
    private String message;
}