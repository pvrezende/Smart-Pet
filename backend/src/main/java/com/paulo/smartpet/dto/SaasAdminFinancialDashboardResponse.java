package com.paulo.smartpet.dto;

import java.math.BigDecimal;
import java.util.List;

public record SaasAdminFinancialDashboardResponse(
        Long totalStores,
        Long totalChargeableStores,
        Long totalOverdueStores,
        BigDecimal totalEstimatedMonthlyRevenue,
        List<SaasBillingStatusSummaryResponse> storesByBillingStatus,
        List<SaasFinancialPlanSummaryResponse> revenueByPlan,
        List<SaasUpcomingBillingResponse> upcomingBillings,
        List<SaasFinancialStoreSummaryResponse> stores
) {
}