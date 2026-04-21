package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.SubscriptionPlan;

import java.math.BigDecimal;

public record SaasFinancialPlanSummaryResponse(
        SubscriptionPlan plan,
        Long storesCount,
        BigDecimal estimatedMonthlyRevenue
) {
}