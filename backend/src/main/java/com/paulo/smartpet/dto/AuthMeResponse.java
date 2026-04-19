package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.UserRole;

public record AuthMeResponse(
        Long id,
        String name,
        String username,
        UserRole role,
        Long storeId,
        String storeName,
        Boolean active
) {
}