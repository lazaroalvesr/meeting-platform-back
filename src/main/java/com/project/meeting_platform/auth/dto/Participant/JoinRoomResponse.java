package com.project.meeting_platform.auth.dto.Participant;

import com.project.meeting_platform.Enum.Participant.ParticipantRole;
import com.project.meeting_platform.Model.Participant;

import java.util.UUID;

public record JoinRoomResponse(
        UUID participantId,
        String displayName,
        ParticipantRole role
) {
    public static JoinRoomResponse from(Participant participant) {
        return new JoinRoomResponse(
                participant.getId(),
                participant.getDisplayName(),
                participant.getRole()
        );
    }
}