package com.okane.dto.responseDto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationResponseDTO {
    private BigDecimal montantEnvoye;
    private BigDecimal fraisFixe;
    private BigDecimal fraisVariable;
    private BigDecimal fraisTotal;
    private BigDecimal montantRecu;
    private Double partAgence;
    private Double partCentrale;
    private String corridorDescription;
    private String message;
}