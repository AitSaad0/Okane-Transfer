package com.okane.dto.requestDto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrilleTarifaireRequestDTO {
    private Long corridorId;
    private BigDecimal montantMin;
    private BigDecimal montantMax;
    private BigDecimal fraisFixe;
    private Double pourcentageFrais;
    private Double partAgence;
}