package com.paulo.smartpet.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        Long totalProducts,
        Long totalCustomers,
        Long totalSales,
        Long completedSales,
        Long canceledSales,
        BigDecimal stockValue,
        Long lowStockCount,
        BigDecimal salesAmountToday,
        BigDecimal salesAmountWeek,
        BigDecimal salesAmountMonth,
        Long salesCountToday,
        Long salesCountWeek,
        Long salesCountMonth
) {
}