package com.project.meeting_platform.realtime;

import com.project.meeting_platform.Enum.Participant.ParticipantRole;

import java.util.UUID;

public record ParticipantPresenceEvent(
        String type,
        UUID participantId,
        String displayName,
        ParticipantRole role
) {
}
