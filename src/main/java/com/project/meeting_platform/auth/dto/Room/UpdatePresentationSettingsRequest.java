package com.project.meeting_platform.auth.dto.Room;

public record UpdatePresentationSettingsRequest(
        boolean scrollLocked,
        boolean presentationActive
) {
}
