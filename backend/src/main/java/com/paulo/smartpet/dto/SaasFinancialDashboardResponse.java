package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;

import java.math.BigDecimal;
import java.util.Map;

public record SaasFinancialDashboardResponse(
        long totalStores,
        long totalSubscriptions,
        long activeSubscriptions,
        long trialSubscriptions,
        long overdueSubscriptions,
        long canceledSubscriptions,
        BigDecimal estimatedMonthlyRevenue,
        BigDecimal activeMonthlyRevenue,
        BigDecimal overdueMonthlyRevenue,
        BigDecimal averageTicket,
        Map<SubscriptionPlan, Long> subscriptionsByPlan,
        Map<SubscriptionStatus, Long> subscriptionsByStatus,
        Map<BillingStatus, Long> subscriptionsByBillingStatus
) {
}