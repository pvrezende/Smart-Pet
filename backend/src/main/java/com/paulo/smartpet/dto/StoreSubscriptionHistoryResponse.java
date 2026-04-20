package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;

import java.time.LocalDateTime;

public record StoreSubscriptionHistoryResponse(
        Long id,
        Long storeId,
        String storeName,
        SubscriptionPlan previousPlan,
        SubscriptionPlan newPlan,
        SubscriptionStatus previousStatus,
        SubscriptionStatus newStatus,
        String notes,
        LocalDateTime changedAt
) {
}