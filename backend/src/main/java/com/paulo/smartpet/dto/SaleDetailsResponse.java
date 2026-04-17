package com.paulo.smartpet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleDetailsResponse(
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
        List<SaleItemResponse> items
) {
}