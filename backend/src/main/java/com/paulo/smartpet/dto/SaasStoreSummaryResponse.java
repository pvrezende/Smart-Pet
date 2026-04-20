package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;

public record SaasStoreSummaryResponse(
        Long storeId,
        String storeName,
        String storeCode,
        Boolean storeActive,
        SubscriptionPlan plan,
        SubscriptionStatus status,
        Boolean inTrial,
        Boolean activeAccess,
        Long usersCount
) {
}