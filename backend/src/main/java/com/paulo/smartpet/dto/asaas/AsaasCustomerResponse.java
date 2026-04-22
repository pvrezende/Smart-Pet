package com.paulo.smartpet.dto.asaas;

public record AsaasCustomerResponse(
        String id,
        String name,
        String cpfCnpj,
        String email,
        String phone,
        String mobilePhone
) {
}