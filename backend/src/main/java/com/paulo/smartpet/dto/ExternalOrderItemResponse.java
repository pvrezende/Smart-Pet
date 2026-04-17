package com.paulo.smartpet.dto;

import java.math.BigDecimal;

public record ExternalOrderItemResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}