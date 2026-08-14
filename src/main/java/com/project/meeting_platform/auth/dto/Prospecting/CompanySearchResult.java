package com.project.meeting_platform.auth.dto.Prospecting;

public record CompanySearchResult(
        String providerPlaceId,
        String name,
        String category,
        String city,
        String state,
        String publicAddress,
        String website,
        String phone,
        String sourceUrl
) {
}
