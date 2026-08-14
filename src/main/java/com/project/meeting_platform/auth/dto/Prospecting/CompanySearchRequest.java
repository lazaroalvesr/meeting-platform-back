package com.project.meeting_platform.auth.dto.Prospecting;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanySearchRequest(
        @NotBlank(message = "Informe o segmento.")
        @Size(max = 120, message = "O segmento deve ter no máximo 120 caracteres.")
        String segment,

        @NotBlank(message = "Informe a cidade.")
        @Size(max = 120, message = "A cidade deve ter no máximo 120 caracteres.")
        String city,

        @NotBlank(message = "Informe a sigla do estado.")
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "Use a sigla do estado com duas letras, por exemplo MG.")
        String state,

        @Min(value = 1, message = "A quantidade deve ser no mínimo 1.")
        @Max(value = 20, message = "A quantidade máxima por busca é 20.")
        int quantity
) {
}
