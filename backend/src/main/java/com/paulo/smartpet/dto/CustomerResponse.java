package com.paulo.smartpet.dto;

public record CustomerResponse(
        Long id,
        String name,
        String cpf,
        String phone,
        String email,
        String address,
        Long storeId,
        String storeName,
        Boolean active
) {
}