package com.paulo.smartpet.dto;

import java.math.BigDecimal;

public record DashboardResponse(Long totalProducts, Long totalCustomers, Long salesCount, BigDecimal stockValue, Long lowStockCount) {
}
