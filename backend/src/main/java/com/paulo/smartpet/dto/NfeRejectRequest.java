package com.paulo.smartpet.dto;

import jakarta.validation.constraints.NotBlank;

public record NfeRejectRequest(
        @NotBlank(message = "Mensagem de rejeição é obrigatória")
        String errorMessage,

        String environment
) {
}