package com.okane.reports.controller.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorridorPerformanceDto {

    private String corridor;

    private Long totalTransactions;

    private BigDecimal totalVolume;

    private BigDecimal averageTransfer;

    private BigDecimal generatedFees;
}