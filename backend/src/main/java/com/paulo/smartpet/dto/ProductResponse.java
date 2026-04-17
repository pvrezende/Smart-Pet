package com.paulo.smartpet.dto;

public record ProductResponse(
        Long id,
        String name,
        String animalType,
        String brand,
        Double weight,
        Double costPrice,
        Double salePrice,
        Integer stock,
        Integer minimumStock,
        String barcode,
        Long storeId,
        String storeName,
        Boolean active,
        Boolean lowStock
) {
}