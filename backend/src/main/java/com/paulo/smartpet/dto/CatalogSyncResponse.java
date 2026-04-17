package com.paulo.smartpet.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CatalogSyncResponse(
        Long storeId,
        String storeCode,
        String storeName,
        LocalDateTime generatedAt,
        Integer count,
        List<CatalogProductResponse> products
) {
}