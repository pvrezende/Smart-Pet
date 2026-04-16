package com.paulo.smartpet.dto;

import java.math.BigDecimal;

public record SalesHistorySummaryResponse(
        Long salesCount,
        Long itemsCount,
        BigDecimal grossAmount,
        BigDecimal discountAmount,
        BigDecimal netAmount,
        Long completedSales,
        Long canceledSales
) {
}