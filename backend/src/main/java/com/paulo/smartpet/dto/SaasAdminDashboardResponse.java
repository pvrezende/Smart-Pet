package com.paulo.smartpet.dto;

import java.util.List;

public record SaasAdminDashboardResponse(
        Long totalStores,
        Long totalActiveStores,
        Long totalInactiveStores,
        Long totalUsers,
        Long totalTrialStores,
        Long totalSuspendedStores,
        Long totalCanceledStores,
        List<SaasPlanSummaryResponse> storesByPlan,
        List<SaasStatusSummaryResponse> storesByStatus,
        List<SaasStoreSummaryResponse> stores
) {
}