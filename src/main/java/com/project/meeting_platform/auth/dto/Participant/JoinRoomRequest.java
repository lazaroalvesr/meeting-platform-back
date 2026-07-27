package com.project.meeting_platform.auth.dto.Participant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRoomRequest(
        @NotBlank(message = "Seu nome é obrigatório.")
        @Size(max = 80, message = "Seu nome deve ter no máximo 80 caracteres.")
        String displayName
) {
}
