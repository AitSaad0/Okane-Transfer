package com.okane.dto.requestDto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationRequestDTO {
    private Long corridorId;
    private BigDecimal montant;
}