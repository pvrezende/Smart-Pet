package com.paulo.smartpet.dto;

public record NfeStatusResponse(
        Long saleId,
        String saleStatus,
        String fiscalStatus,
        String nfeNumber,
        String nfeSeries,
        String nfeAccessKey,
        String nfeEnvironment,
        String nfeErrorMessage
) {
}