package com.okane.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThresholdRequest {

    @NotNull
    private BigDecimal sarThreshold;
}