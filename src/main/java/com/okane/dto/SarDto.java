package com.okane.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SarDto {

    private Long id;

    private String referenceCode;

    private String reason;

    private BigDecimal thresholdAmount;

    private BigDecimal transferAmount;

    private String status;

    private String transferCode;

    private LocalDateTime createdAt;
}