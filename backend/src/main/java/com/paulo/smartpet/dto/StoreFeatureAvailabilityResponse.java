package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.SaasFeature;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;

import java.util.List;

public record StoreFeatureAvailabilityResponse(
        Long storeId,
        String storeName,
        SubscriptionPlan plan,
        SubscriptionStatus status,
        List<SaasFeature> enabledFeatures,
        List<SaasFeature> disabledFeatures
) {
}