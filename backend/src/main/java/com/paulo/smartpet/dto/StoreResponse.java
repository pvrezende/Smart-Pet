package com.paulo.smartpet.dto;

public record StoreResponse(
        Long id,
        String name,
        String code,
        String address,
        String phone,
        Boolean active
) {
}