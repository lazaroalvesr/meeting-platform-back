package com.project.meeting_platform.auth.dto.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Informe a senha atual.")
        String currentPassword,

        @NotBlank(message = "Informe a nova senha.")
        @Size(min = 12, message = "A nova senha deve ter pelo menos 12 caracteres.")
        String newPassword
) {
}
