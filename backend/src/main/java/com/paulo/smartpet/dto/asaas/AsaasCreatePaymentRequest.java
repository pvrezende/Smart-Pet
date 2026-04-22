package com.paulo.smartpet.dto.asaas;

import java.math.BigDecimal;

public record AsaasCreatePaymentRequest(
        String customer,
        String billingType,
        BigDecimal value,
        String dueDate,
        String description,
        String externalReference
) {
}