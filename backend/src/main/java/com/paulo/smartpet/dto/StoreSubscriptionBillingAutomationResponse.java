package com.paulo.smartpet.dto;

import java.time.LocalDateTime;

public record StoreSubscriptionBillingAutomationResponse(
        Long totalProcessed,
        Long totalUpdated,
        Long totalOverdueStores,
        Long totalPendingStores,
        LocalDateTime processedAt
) {
}