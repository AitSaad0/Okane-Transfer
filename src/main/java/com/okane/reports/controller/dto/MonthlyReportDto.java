package com.okane.reports.controller.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyReportDto {

    private String month;

    private String corridor;

    private Long transactionCount;

    private BigDecimal totalVolume;

    private BigDecimal totalRevenue;
}