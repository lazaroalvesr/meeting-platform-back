package com.project.meeting_platform.auth.dto.Prospecting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AnalyzeLeadRequest(
        @NotBlank(message = "Informe o nome da empresa.")
        @Size(max = 200)
        String name,

        @Size(max = 200)
        String category,

        @NotBlank(message = "Informe a cidade.")
        @Size(max = 120)
        String city,

        @NotBlank(message = "Informe a sigla do estado.")
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "Use a sigla do estado com duas letras.")
        String state,

        @Size(max = 500)
        String publicAddress,

        @Size(max = 500)
        String website,

        @Size(max = 50)
        String phone,

        @Size(max = 500)
        String sourceUrl
) {
}
