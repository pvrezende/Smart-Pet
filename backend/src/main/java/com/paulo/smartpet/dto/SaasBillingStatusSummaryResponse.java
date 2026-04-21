package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.BillingStatus;

public record SaasBillingStatusSummaryResponse(
        BillingStatus billingStatus,
        Long storesCount
) {
}