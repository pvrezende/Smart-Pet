package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;

public record StorePlanLimitsResponse(
        Long storeId,
        String storeName,
        SubscriptionPlan plan,
        SubscriptionStatus status,
        Integer usersLimit,
        Long currentActiveUsers,
        Long availableUserSlots,
        Boolean canCreateUsers
) {
}