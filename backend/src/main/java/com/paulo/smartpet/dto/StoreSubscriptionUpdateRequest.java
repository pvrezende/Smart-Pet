package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record StoreSubscriptionUpdateRequest(

        @NotNull(message = "Plano é obrigatório")
        SubscriptionPlan plan,

        @NotNull(message = "Status da assinatura é obrigatório")
        SubscriptionStatus status,

        LocalDateTime startsAt,
        LocalDateTime trialEndsAt,
        LocalDateTime subscriptionEndsAt,

        @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
        String notes
) {
}