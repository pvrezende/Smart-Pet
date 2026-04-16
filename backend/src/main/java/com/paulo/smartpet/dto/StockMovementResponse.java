package com.paulo.smartpet.dto;

import java.time.LocalDateTime;

public record StockMovementResponse(
        Long id,
        Long productId,
        String productName,
        String movementType,
        Integer quantity,
        Integer previousStock,
        Integer currentStock,
        String observation,
        LocalDateTime movementDate
) {
}