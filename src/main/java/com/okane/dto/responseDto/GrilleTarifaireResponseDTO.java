package com.okane.dto.responseDto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrilleTarifaireResponseDTO {
    private Long id;
    private BigDecimal montantMin;
    private BigDecimal montantMax;
    private BigDecimal fraisFixe;
    private Double pourcentageFrais;
    private Double partAgence;

    // Corridor info
    private Long corridorId;
    private String corridorPaysOrigineNom;
    private String corridorPaysDestinationNom;
    private String corridorDeviseSourceCode;
    private String corridorDeviseDestinationCode;
}