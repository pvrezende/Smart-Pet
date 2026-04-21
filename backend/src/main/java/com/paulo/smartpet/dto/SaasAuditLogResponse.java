package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.UserRole;

import java.time.LocalDateTime;

public record SaasAuditLogResponse(
        Long id,
        Long storeId,
        Long userId,
        String username,
        UserRole userRole,
        String action,
        String details,
        LocalDateTime createdAt
) {
}