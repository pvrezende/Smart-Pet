package com.paulo.smartpet.dto.asaas;

public record AsaasCreateCustomerRequest(
        String name,
        String cpfCnpj,
        String email,
        String phone,
        String mobilePhone
) {
}