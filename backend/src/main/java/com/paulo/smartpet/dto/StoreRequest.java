package com.paulo.smartpet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StoreRequest(

        @NotBlank(message = "Nome da loja é obrigatório")
        @Size(max = 150, message = "Nome da loja deve ter no máximo 150 caracteres")
        String name,

        @Size(max = 20, message = "Código da loja deve ter no máximo 20 caracteres")
        @Pattern(
                regexp = "^$|^[A-Za-z0-9_-]+$",
                message = "Código da loja deve conter apenas letras, números, hífen ou underscore"
        )
        String code,

        @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
        String address,

        @Pattern(
                regexp = "^$|^\\d{10,11}$",
                message = "Telefone deve conter 10 ou 11 dígitos numéricos"
        )
        String phone
) {
}