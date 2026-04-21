package com.paulo.smartpet.dto;

import java.time.LocalDateTime;

public record SaasNotificationResponse(
        Long id,
        Long storeId,
        String type,
        String title,
        String message,
        Boolean read,
        LocalDateTime createdAt
) {
}