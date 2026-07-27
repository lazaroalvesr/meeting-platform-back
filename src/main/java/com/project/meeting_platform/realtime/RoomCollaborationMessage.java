package com.project.meeting_platform.realtime;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public record RoomCollaborationMessage(
        UUID participantId,
        String type,
        JsonNode payload
) {
}
