package com.paulo.smartpet.dto.asaas;

import java.math.BigDecimal;

public record AsaasWebhookEvent(
        String event,
        Payment payment
) {

    public record Payment(
            String id,
            String status,
            String billingType,
            BigDecimal value,
            String dueDate,
            String paymentDate,
            String description,
            String externalReference,
            String customer
    ) {}
}