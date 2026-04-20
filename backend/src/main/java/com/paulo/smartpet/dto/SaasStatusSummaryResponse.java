package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.SubscriptionStatus;

public record SaasStatusSummaryResponse(
        SubscriptionStatus status,
        Long storesCount
) {
}