package com.project.meeting_platform.auth.dto.User;

public record AccountResponse(
        String name,
        String email,
        String accessToken,
        String tokenType
) {
}
