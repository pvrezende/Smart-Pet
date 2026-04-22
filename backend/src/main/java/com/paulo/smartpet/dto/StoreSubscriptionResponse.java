package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.PaymentProvider;
import com.paulo.smartpet.entity.SaasFeature;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StoreSubscriptionResponse(
        Long id,
        Long storeId,
        String storeName,
        SubscriptionPlan plan,
        SubscriptionStatus status,
        BillingStatus billingStatus,
        LocalDateTime startsAt,
        LocalDateTime trialEndsAt,
        LocalDateTime subscriptionEndsAt,
        Integer billingDay,
        LocalDate nextBillingDate,
        BigDecimal monthlyPrice,
        String notes,
        PaymentProvider paymentProvider,
        String externalCustomerId,
        String externalSubscriptionId,
        String externalBillingId,
        String externalBillingStatus,
        Boolean inTrial,
        Boolean activeAccess,
        List<SaasFeature> enabledFeatures,
        List<SaasFeature> disabledFeatures,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}