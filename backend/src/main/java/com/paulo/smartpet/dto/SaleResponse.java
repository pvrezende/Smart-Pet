package com.paulo.smartpet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaleResponse(
        Long id,
        LocalDateTime saleDate,
        SaleCustomerResponse customer,
        BigDecimal totalAmount,
        BigDecimal discount,
        BigDecimal finalAmount,
        String paymentMethod,
        String status,
        String notes,
        String source,
        String externalId,
        Integer itemsCount
) {
}