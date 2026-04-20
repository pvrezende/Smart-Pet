package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.SaasFeature;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record StoreSubscriptionResponse(
        Long id,
        Long storeId,
        String storeName,
        SubscriptionPlan plan,
        SubscriptionStatus status,
        LocalDateTime startsAt,
        LocalDateTime trialEndsAt,
        LocalDateTime subscriptionEndsAt,
        String notes,
        Boolean inTrial,
        Boolean activeAccess,
        List<SaasFeature> enabledFeatures,
        List<SaasFeature> disabledFeatures,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}