package com.project.meeting_platform.realtime;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public record RoomSignalMessage(
        UUID fromParticipantId,
        UUID toParticipantId,
        String type,
        JsonNode payload
) {
}
