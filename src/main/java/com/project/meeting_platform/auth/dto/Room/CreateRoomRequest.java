package com.project.meeting_platform.auth.dto.Room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
        @NotBlank(message = "O título é obrigatório.")
        @Size(max = 120, message = "O título deve ter no máximo 120 caracteres.")
        String title,

        @NotBlank(message = "A URL do projeto é obrigatória.")
        @Size(max = 2048, message = "A URL deve ter no máximo 2048 caracteres.")
        @Pattern(
                regexp = "https?://.+",
                message = "Informe uma URL que comece com http:// ou https://."
        )
        String projectUrl
) {
}
