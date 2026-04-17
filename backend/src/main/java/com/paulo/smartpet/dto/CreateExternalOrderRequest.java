package com.paulo.smartpet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateExternalOrderRequest(
        @NotNull(message = "Loja é obrigatória")
        Long storeId,

        @NotBlank(message = "Origem é obrigatória")
        String source,

        @NotBlank(message = "Identificador externo é obrigatório")
        String externalId,

        @NotBlank(message = "Nome do cliente é obrigatório")
        String customerName,

        String customerDocument,
        String customerPhone,

        @NotNull(message = "Itens são obrigatórios")
        @Valid
        List<ExternalOrderItemRequest> items,

        @NotBlank(message = "Forma de pagamento é obrigatória")
        String paymentMethod,

        BigDecimal discount,
        String notes
) {
}