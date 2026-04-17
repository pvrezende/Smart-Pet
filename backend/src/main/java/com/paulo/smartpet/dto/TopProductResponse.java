package com.paulo.smartpet.dto;

import java.math.BigDecimal;

public record TopProductResponse(
        Long productId,
        String productName,
        Long quantitySold,
        BigDecimal totalAmount
) {
}