package com.okane.dto.external;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class ExchangeRateApiResponse {
    private String base;
    private String date;
    private Map<String, BigDecimal> rates;
}