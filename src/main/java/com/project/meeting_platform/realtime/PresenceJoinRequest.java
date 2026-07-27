package com.project.meeting_platform.realtime;

import java.util.UUID;

public record PresenceJoinRequest(
        UUID participantId
) {
}
