package com.paulo.smartpet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoreOnboardingRequest(

        @NotBlank(message = "Nome da loja é obrigatório")
        @Size(max = 120, message = "Nome da loja deve ter no máximo 120 caracteres")
        String storeName,

        @NotBlank(message = "Código da loja é obrigatório")
        @Size(max = 40, message = "Código da loja deve ter no máximo 40 caracteres")
        String storeCode,

        @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
        String storeAddress,

        @Size(max = 30, message = "Telefone deve ter no máximo 30 caracteres")
        String storePhone,

        @NotBlank(message = "Nome do administrador é obrigatório")
        @Size(max = 120, message = "Nome do administrador deve ter no máximo 120 caracteres")
        String adminName,

        @NotBlank(message = "Username do administrador é obrigatório")
        @Size(max = 120, message = "Username do administrador deve ter no máximo 120 caracteres")
        String adminUsername,

        @NotBlank(message = "Senha do administrador é obrigatória")
        @Size(min = 6, max = 120, message = "Senha do administrador deve ter entre 6 e 120 caracteres")
        String adminPassword
) {
}