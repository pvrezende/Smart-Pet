package com.paulo.smartpet.dto;

import jakarta.validation.constraints.NotBlank;

public record NfeAuthorizeRequest(
        @NotBlank(message = "Número da NF-e é obrigatório")
        String nfeNumber,

        @NotBlank(message = "Série da NF-e é obrigatória")
        String nfeSeries,

        @NotBlank(message = "Chave de acesso da NF-e é obrigatória")
        String nfeAccessKey,

        @NotBlank(message = "Ambiente da NF-e é obrigatório")
        String environment
) {
}