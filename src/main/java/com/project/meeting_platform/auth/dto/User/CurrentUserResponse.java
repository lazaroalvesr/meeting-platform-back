package com.project.meeting_platform.auth.dto.User;

public record CurrentUserResponse(
        String email,
        String role
) {
}
