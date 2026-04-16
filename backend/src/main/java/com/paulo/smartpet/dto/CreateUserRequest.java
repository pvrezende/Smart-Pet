package com.paulo.smartpet.dto;

import com.paulo.smartpet.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank(message = "Nome do usuário é obrigatório")
        @Size(max = 120, message = "Nome do usuário deve ter no máximo 120 caracteres")
        String name,

        @NotBlank(message = "Username é obrigatório")
        @Size(max = 120, message = "Username deve ter no máximo 120 caracteres")
        String username,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, max = 120, message = "Senha deve ter entre 6 e 120 caracteres")
        String password,

        @NotNull(message = "Perfil é obrigatório")
        UserRole role
) {
}