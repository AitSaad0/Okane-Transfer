package com.okane.dto.responseDto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TauxChangeResponseDTO {
    private Long id;
    private BigDecimal taux;
    private String source;
    private LocalDateTime dateMiseAJour;

    private Long corridorId;
    private String paysOrigineNom;
    private String paysDestinationNom;
    private String deviseSourceCode;
    private String deviseDestinationCode;
    private String deviseSourceSymbole;
    private String deviseDestinationSymbole;
}