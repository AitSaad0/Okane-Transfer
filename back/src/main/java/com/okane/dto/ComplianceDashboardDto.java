package com.okane.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceDashboardDto {

    private Long totalTransfers;

    private Long suspiciousTransfers;

    private Long totalSarReports;

    private Long openSarReports;

    private Double suspiciousRate;
}