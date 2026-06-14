package com.okane.dto.requestDto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorridorRequestDTO {
    private Long paysOrigineId;
    private Long paysDestinationId;
    private Long deviseSourceId;
    private Long deviseDestinationId;
    private BigDecimal tauxChange;
    private Boolean actif;
}