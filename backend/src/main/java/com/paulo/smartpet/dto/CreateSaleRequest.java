package com.paulo.smartpet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateSaleRequest(

        Long customerId,

        @NotEmpty(message = "A venda deve possuir ao menos um item")
        List<@Valid SaleItemRequest> items,

        @NotBlank(message = "Forma de pagamento é obrigatória")
        @Size(max = 50, message = "Forma de pagamento deve ter no máximo 50 caracteres")
        String paymentMethod,

        @DecimalMin(value = "0.0", inclusive = true, message = "Desconto não pode ser negativo")
        BigDecimal discount,

        @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
        String notes
) {
}