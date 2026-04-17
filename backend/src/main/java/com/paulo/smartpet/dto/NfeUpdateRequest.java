package com.paulo.smartpet.dto;

import jakarta.validation.constraints.NotBlank;

public record NfeUpdateRequest(
        @NotBlank(message = "Status fiscal é obrigatório")
        String fiscalStatus,

        String nfeNumber,
        String nfeSeries,
        String nfeAccessKey,
        String nfeEnvironment,
        String nfeErrorMessage
) {
}