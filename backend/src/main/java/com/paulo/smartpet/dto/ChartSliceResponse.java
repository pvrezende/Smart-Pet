package com.paulo.smartpet.dto;

import java.math.BigDecimal;

public record ChartSliceResponse(
        String label,
        Long count,
        BigDecimal amount
) {
}