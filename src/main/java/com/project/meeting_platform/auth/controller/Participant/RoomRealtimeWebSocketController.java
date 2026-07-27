package com.project.meeting_platform.auth.controller.Participant;

import com.project.meeting_platform.auth.Service.Participant.ParticipantService;
import com.project.meeting_platform.realtime.RoomCollaborationMessage;
import com.project.meeting_platform.realtime.RoomSignalMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class RoomRealtimeWebSocketController {

    private final ParticipantService participantService;
    private final SimpMessagingTemplate messagingTemplate;

    public RoomRealtimeWebSocketController(
            ParticipantService participantService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.participantService = participantService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/rooms/{slug}/signal")
    public void signal(
            @DestinationVariable String slug,
            @Payload RoomSignalMessage message
    ) {
        participantService.requireActiveParticipant(
                slug,
                message.fromParticipantId()
        );

        participantService.requireActiveParticipant(
                slug,
                message.toParticipantId()
        );

        messagingTemplate.convertAndSend(
                "/topic/rooms/" + slug + "/signal",
                message
        );
    }

    @MessageMapping("/rooms/{slug}/collaboration")
    public void collaboration(
            @DestinationVariable String slug,
            @Payload RoomCollaborationMessage message
    ) {
        participantService.requireActiveParticipant(
                slug,
                message.participantId()
        );

        messagingTemplate.convertAndSend(
                "/topic/rooms/" + slug + "/collaboration",
                message
        );
    }
}
