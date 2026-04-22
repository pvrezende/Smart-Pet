package com.paulo.smartpet.dto;

import java.time.LocalDateTime;

public record SaasBillingAutomationResponse(
        long totalSubscriptionsChecked,
        long totalChargesGenerated,
        long totalSkipped,
        LocalDateTime executedAt
) {
}