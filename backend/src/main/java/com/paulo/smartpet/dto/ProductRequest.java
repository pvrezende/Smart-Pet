package com.paulo.smartpet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductRequest(

        @NotBlank(message = "Nome do produto é obrigatório")
        @Size(max = 150, message = "Nome do produto deve ter no máximo 150 caracteres")
        String name,

        @NotBlank(message = "Tipo do animal é obrigatório")
        @Size(max = 30, message = "Tipo do animal deve ter no máximo 30 caracteres")
        String animalType,

        @NotBlank(message = "Marca é obrigatória")
        @Size(max = 100, message = "Marca deve ter no máximo 100 caracteres")
        String brand,

        @NotNull(message = "Peso é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "Peso deve ser maior que zero")
        Double weight,

        @NotNull(message = "Preço de custo é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "Preço de custo deve ser maior que zero")
        Double costPrice,

        @NotNull(message = "Preço de venda é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "Preço de venda deve ser maior que zero")
        Double salePrice,

        @NotNull(message = "Estoque é obrigatório")
        @Min(value = 0, message = "Estoque não pode ser negativo")
        Integer stock,

        @NotNull(message = "Estoque mínimo é obrigatório")
        @Min(value = 0, message = "Estoque mínimo não pode ser negativo")
        Integer minimumStock,

        @Size(max = 100, message = "Código de barras deve ter no máximo 100 caracteres")
        @Pattern(
                regexp = "^$|^[0-9A-Za-z\\-]+$",
                message = "Código de barras deve conter apenas letras, números ou hífen"
        )
        String barcode
) {
}