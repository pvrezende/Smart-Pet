package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.PaymentProvider;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StoreSubscriptionPaymentLinkResponse(
        Long storeId,
        String storeName,
        PaymentProvider paymentProvider,
        String externalCustomerId,
        String externalSubscriptionId,
        String externalBillingId,
        String externalBillingStatus,
        String billingType,
        BigDecimal value,
        LocalDate dueDate,
        String invoiceUrl,
        String bankSlipUrl,
        String transactionReceiptUrl
) {
}