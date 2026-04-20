package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StoreSubscriptionUpdateRequest(

        @NotNull(message = "Plano é obrigatório")
        SubscriptionPlan plan,

        @NotNull(message = "Status da assinatura é obrigatório")
        SubscriptionStatus status,

        @NotNull(message = "Status financeiro é obrigatório")
        BillingStatus billingStatus,

        LocalDateTime startsAt,
        LocalDateTime trialEndsAt,
        LocalDateTime subscriptionEndsAt,

        @Min(value = 1, message = "Dia de cobrança deve ser entre 1 e 28")
        @Max(value = 28, message = "Dia de cobrança deve ser entre 1 e 28")
        Integer billingDay,

        LocalDate nextBillingDate,

        @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
        String notes
) {
}