package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.BillingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record StoreSubscriptionBillingHistoryResponse(
        Long id,
        Long storeId,
        String storeName,
        BillingStatus previousBillingStatus,
        BillingStatus newBillingStatus,
        BigDecimal previousMonthlyPrice,
        BigDecimal newMonthlyPrice,
        Integer previousBillingDay,
        Integer newBillingDay,
        LocalDate previousNextBillingDate,
        LocalDate newNextBillingDate,
        String notes,
        LocalDateTime changedAt
) {
}