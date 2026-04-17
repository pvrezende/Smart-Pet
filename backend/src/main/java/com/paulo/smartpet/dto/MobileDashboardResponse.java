package com.paulo.smartpet.dto;

import java.math.BigDecimal;
import java.util.List;

public record MobileDashboardResponse(
        Long storeId,
        String storeName,
        Long totalProducts,
        Long totalCustomers,
        Long totalSales,
        Long lowStockCount,
        BigDecimal salesAmountToday,
        BigDecimal salesAmountMonth,
        List<ProductResponse> lowStockProducts,
        List<SaleResponse> recentSales
) {
}