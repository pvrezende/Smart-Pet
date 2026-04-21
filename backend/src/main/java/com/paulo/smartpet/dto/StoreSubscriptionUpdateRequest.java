package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.PaymentProvider;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StoreSubscriptionUpdateRequest(
        @NotNull(message = "Plano é obrigatório")
        SubscriptionPlan plan,

        @NotNull(message = "Status da assinatura é obrigatório")
        SubscriptionStatus status,

        @NotNull(message = "Status financeiro é obrigatório")
        BillingStatus billingStatus,

        @NotNull(message = "Data de início é obrigatória")
        LocalDateTime startsAt,

        LocalDateTime trialEndsAt,
        LocalDateTime subscriptionEndsAt,

        Integer billingDay,
        LocalDate nextBillingDate,
        String notes,

        PaymentProvider paymentProvider,
        String externalSubscriptionId,
        String externalBillingId,
        String externalBillingStatus
) {
}