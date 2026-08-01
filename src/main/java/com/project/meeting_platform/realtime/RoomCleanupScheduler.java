package com.project.meeting_platform.realtime;

import com.project.meeting_platform.auth.Service.Participant.ParticipantService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RoomCleanupScheduler {

    private final ParticipantService participantService;
    private final long inactiveParticipantTimeoutSeconds;

    public RoomCleanupScheduler(
            ParticipantService participantService,
            @Value("${app.rooms.inactive-participant-timeout-seconds:120}") long inactiveParticipantTimeoutSeconds
    ) {
        this.participantService = participantService;
        this.inactiveParticipantTimeoutSeconds = inactiveParticipantTimeoutSeconds;
    }

    @Scheduled(fixedDelayString = "${app.rooms.cleanup-interval-milliseconds:30000}")
    public void closeAbandonedRooms() {
        participantService.expireInactiveParticipantsAndCloseEmptyRooms(
                inactiveParticipantTimeoutSeconds
        );
    }
}
