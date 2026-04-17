package com.paulo.smartpet.dto;

import java.math.BigDecimal;

public record StoreStockSummaryResponse(
        Long storeId,
        String storeName,
        Long totalProducts,
        Long activeProducts,
        Long lowStockProducts,
        BigDecimal estimatedStockValue
) {
}