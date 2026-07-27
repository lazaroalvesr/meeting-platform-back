package com.project.meeting_platform.auth.dto.User;

public record LoginResponse(
        String accessToken,
        String tokenType
) {
}
