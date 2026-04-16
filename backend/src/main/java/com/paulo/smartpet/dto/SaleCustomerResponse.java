package com.paulo.smartpet.dto;

public record SaleCustomerResponse(
        Long id,
        String name,
        String cpf
) {
}