package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.UserRole;

public record LoginResponse(
        Long id,
        String name,
        String username,
        UserRole role,
        Boolean active,
        String token,
        String message
) {
}