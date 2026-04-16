package com.paulo.smartpet.dto;

public record CompanySettingsResponse(
        Long id,
        String tradeName,
        String legalName,
        String cnpj,
        String phone,
        String email,
        String address,
        String receiptFooterMessage
) {
}