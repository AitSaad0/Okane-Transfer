package com.okane.dto.reports;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyReportDto {

    private String corridor;

    private Long transactionCount;

    private BigDecimal totalVolume;

    private BigDecimal totalRevenue;

    private BigDecimal agencyCommission;

    private BigDecimal centralCommission;
}