package com.okane.dto.responseDto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorridorResponseDTO {
    private Long id;
    private BigDecimal tauxChange;
    private Boolean actif;

    private Long paysOrigineId;
    private String paysOrigineNom;

    private Long paysDestinationId;
    private String paysDestinationNom;

    private Long deviseSourceId;
    private String deviseSourceCode;
    private String deviseSourceSymbole;

    private Long deviseDestinationId;
    private String deviseDestinationCode;
    private String deviseDestinationSymbole;
}