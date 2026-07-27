package com.project.meeting_platform.auth.dto.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail é inválido.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        String password
) {
}
