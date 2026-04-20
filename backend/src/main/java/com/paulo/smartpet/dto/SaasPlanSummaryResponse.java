package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.SubscriptionPlan;

public record SaasPlanSummaryResponse(
        SubscriptionPlan plan,
        Long storesCount
) {
}