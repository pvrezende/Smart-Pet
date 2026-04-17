package com.paulo.smartpet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ExternalOrderResponse(
        Long id,
        Long storeId,
        String storeName,
        String source,
        String externalId,
        String status,
        String customerName,
        String customerDocument,
        String customerPhone,
        BigDecimal totalAmount,
        BigDecimal discount,
        BigDecimal finalAmount,
        String paymentMethod,
        String notes,
        Long saleId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ExternalOrderItemResponse> items
) {
}