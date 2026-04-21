package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.PaymentProvider;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StoreBillingSummaryResponse(
        Long storeId,
        String storeName,
        SubscriptionPlan plan,
        SubscriptionStatus status,
        BillingStatus billingStatus,
        BigDecimal monthlyPrice,
        Integer billingDay,
        LocalDate nextBillingDate,
        Boolean overdue,
        PaymentProvider paymentProvider,
        String externalSubscriptionId,
        String externalBillingId,
        String externalBillingStatus
) {
}