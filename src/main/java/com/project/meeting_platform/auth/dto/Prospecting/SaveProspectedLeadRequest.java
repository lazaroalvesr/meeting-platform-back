package com.project.meeting_platform.auth.dto.Prospecting;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveProspectedLeadRequest(
        @Size(max = 255) String providerPlaceId,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 200) String category,
        @NotBlank @Size(max = 120) String city,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") String state,
        @Size(max = 500) String publicAddress,
        @Size(max = 500) String website,
        @Size(max = 50) String phone,
        @Size(max = 500) String sourceUrl,
        @Min(0) @Max(100) Integer priorityScore,
        @Size(max = 1000) String openingMessage
) {
}
