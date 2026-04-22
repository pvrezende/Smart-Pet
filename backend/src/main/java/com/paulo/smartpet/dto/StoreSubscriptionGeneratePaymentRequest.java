package com.paulo.smartpet.dto;

import jakarta.validation.constraints.NotBlank;

public record StoreSubscriptionGeneratePaymentRequest(
        @NotBlank(message = "Tipo de cobrança é obrigatório")
        String billingType
) {
}