package com.paulo.smartpet.dto;

import java.time.LocalDateTime;

public record SaasBackendHealthResponse(
        String application,
        String module,
        String status,
        boolean paymentsEnabled,
        boolean webhookEnabled,
        boolean notificationsEnabled,
        boolean billingAutomationEnabled,
        LocalDateTime checkedAt
) {
}