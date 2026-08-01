package com.project.meeting_platform.auth.dto.Client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateClientRequest(
        @NotBlank(message = "O nome do cliente é obrigatório.")
        @Size(max = 120)
        String name,

        @Size(max = 160)
        String companyName,

        @Email(message = "Informe um e-mail válido.")
        @Size(max = 255)
        String email,

        @Size(max = 30)
        String phone,

        @Size(max = 30)
        String document,

        String notes
) {
}
