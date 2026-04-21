package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaasUpcomingBillingResponse(
        Long storeId,
        String storeName,
        String storeCode,
        SubscriptionPlan plan,
        SubscriptionStatus subscriptionStatus,
        BillingStatus billingStatus,
        BigDecimal monthlyPrice,
        Integer billingDay,
        LocalDate nextBillingDate,
        Boolean overdue
) {
}