package com.paulo.smartpet.dto;

import java.math.BigDecimal;
import java.util.List;

public record SalesAnalyticsResponse(
        String periodType,
        BigDecimal totalAmount,
        Long totalSales,
        List<SalesSeriesItemResponse> series,
        List<TopProductResponse> topProducts
) {
}