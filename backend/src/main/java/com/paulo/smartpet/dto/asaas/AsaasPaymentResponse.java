package com.paulo.smartpet.dto.asaas;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AsaasPaymentResponse(
        String id,
        String customer,
        String billingType,
        String status,
        BigDecimal value,
        LocalDate dueDate,
        String description,
        String externalReference,
        String invoiceUrl,
        String bankSlipUrl,
        String transactionReceiptUrl
) {
}