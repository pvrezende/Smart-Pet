package com.paulo.smartpet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanySettingsRequest(

        @NotBlank(message = "Nome fantasia é obrigatório")
        @Size(max = 150, message = "Nome fantasia deve ter no máximo 150 caracteres")
        String tradeName,

        @Size(max = 200, message = "Razão social deve ter no máximo 200 caracteres")
        String legalName,

        @Pattern(
                regexp = "^$|^\\d{14}$",
                message = "CNPJ deve conter exatamente 14 dígitos numéricos"
        )
        String cnpj,

        @Pattern(
                regexp = "^$|^\\d{10,11}$",
                message = "Telefone deve conter 10 ou 11 dígitos numéricos"
        )
        String phone,

        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
        String email,

        @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
        String address,

        @Size(max = 255, message = "Mensagem do rodapé deve ter no máximo 255 caracteres")
        String receiptFooterMessage
) {
}