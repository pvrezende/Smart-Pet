package com.paulo.smartpet.dto;

public record LowStockItemResponse(
        Long productId,
        String productName,
        String animalType,
        String brand,
        Integer stock,
        Integer minimumStock,
        Integer shortage,
        Long storeId,
        String storeName
) {
}