package com.paulo.smartpet.dto;

import java.math.BigDecimal;

public record SalesSeriesItemResponse(
        String label,
        BigDecimal amount,
        Long salesCount
) {
}