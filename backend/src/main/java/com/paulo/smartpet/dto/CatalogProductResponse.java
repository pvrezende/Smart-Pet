package com.paulo.smartpet.dto;

public record CatalogProductResponse(
        Long id,
        String name,
        String animalType,
        String brand,
        Double weight,
        Double salePrice,
        Integer stock,
        String barcode,
        Boolean available,
        Boolean active,
        String storeCode,
        String storeName
) {
}