package com.paulo.smartpet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record IntegrationSaleRequest(
        Long customerId,

        @NotNull(message = "Loja é obrigatória")
        Long storeId,

        @NotBlank(message = "Origem da venda é obrigatória")
        String source,

        @NotBlank(message = "Identificador externo é obrigatório")
        String externalId,

        @NotNull(message = "Itens são obrigatórios")
        List<SaleItemRequest> items,

        @NotBlank(message = "Forma de pagamento é obrigatória")
        String paymentMethod,

        BigDecimal discount,
        String notes
) {
}