package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.UserRole;

public record UserResponse(
        Long id,
        String name,
        String username,
        UserRole role,
        Boolean active
) {
}